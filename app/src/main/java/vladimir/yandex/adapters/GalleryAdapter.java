package vladimir.yandex.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import vladimir.yandex.R;
import vladimir.yandex.activities.GalleryActivity;
import vladimir.yandex.activities.PhotoActivity;
import vladimir.yandex.entity.Result;

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<Result> mCharacters;
    public final int REGULAR_ITEM = 0;
    public final int LOADING_ITEM = 1;
    public boolean ERROR = false;

    public GalleryAdapter() {
        mCharacters = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == REGULAR_ITEM){
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            viewHolder = new RegularViewHolder(view);
        }else if(viewType == LOADING_ITEM){
            View view = inflater.inflate(R.layout.gallery_item_loading, parent, false);
            viewHolder = new LoadingViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        return position == mCharacters.size() ? LOADING_ITEM : REGULAR_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == REGULAR_ITEM){
                final RegularViewHolder regularViewHolder = (RegularViewHolder) holder;
                Glide.with(regularViewHolder.mImage.getContext())
                        .load(mCharacters.get(position).getImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(regularViewHolder.mImage);
                regularViewHolder.mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), PhotoActivity.class);
                        intent.putExtra("URL", mCharacters.get(position).getImage());
                        intent.putExtra("NAME", mCharacters.get(position).getName());
                        v.getContext().startActivity(intent);
                    }
                });
        }
    }


    @Override
    public int getItemCount() {
        return mCharacters.size() + 1;
    }

    /*
        Вспомогательные функции для загрузки данных в адаптер
   _________________________________________________________________________________________________
    */

    public void add(Result result){
        mCharacters.add(result);
        notifyItemInserted(mCharacters.size() - 1);
    }

    public void addAll(List<Result> results){
        for(Result result : results){
            add(result);
        }
    }

    /*
        Вспомогательные функции для сохранения данных после поворота экрана
   _________________________________________________________________________________________________
    */
    public List<Result> getGalleryItems(){
        return mCharacters;
    }

    /*
        Вьюхолдеры
   _________________________________________________________________________________________________
    */
    static class RegularViewHolder extends RecyclerView.ViewHolder{
        ImageView mImage;
        RegularViewHolder(View itemView) {
            super(itemView);
            mImage = itemView.findViewById(R.id.image);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder{
        ProgressBar mProgress;
        LinearLayout mErrorLayouyt;
        TextView mErrorText;
        ImageButton mRetryButton;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            mProgress = itemView.findViewById(R.id.loadmore_progress);
            mErrorLayouyt = itemView.findViewById(R.id.loadmore_errorlayout);
            mErrorText = itemView.findViewById(R.id.loadmore_errortxt);
            mRetryButton = itemView.findViewById(R.id.loadmore_retry);
        }
    }
}