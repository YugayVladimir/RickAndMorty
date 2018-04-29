package vladimir.yandex.activities;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vladimir.yandex.Constants;
import vladimir.yandex.adapters.GalleryAdapter;
import vladimir.yandex.R;
import vladimir.yandex.api.CharactersApi;
import vladimir.yandex.api.CharactersService;
import vladimir.yandex.entity.Reponse;
import vladimir.yandex.entity.Result;

public class GalleryActivity extends AppCompatActivity{

    private GalleryAdapter mAdapter;
    private GridLayoutManager mLayoutManager;
    private RecyclerView mRecycler;
    private boolean isLoading = false;
    private Call<Reponse> mCall;
    private CharactersService mService;
    private String PAGE = "1";
    private Parcelable mRecyclerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecycler = findViewById(R.id.recycler);
        mAdapter = new GalleryAdapter();

        if(savedInstanceState != null){
            PAGE = savedInstanceState.getString(Constants.PAGE);
            mAdapter.addAll(savedInstanceState.<Result>getParcelableArrayList(Constants.DATA));
            mRecyclerState = savedInstanceState.getParcelable(Constants.RECYCLER_STATE);
        }

        mLayoutManager = new GridLayoutManager(this, 2);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(mLayoutManager);

        //пагинация
        mRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = mLayoutManager.getItemCount();
                int visibleItemCount = mLayoutManager.getChildCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                if(dy > 0){
                    if((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && !isLoading
                            && totalItemCount >= 20){
                        loadData();
                    }
                }
            }
        });

        //чтобы вью с прогресс бар была во всю ширину экрана
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup(){
            @Override
            public int getSpanSize(int position) {
                if(mAdapter.getItemViewType(position) == mAdapter.REGULAR_ITEM){
                    return 1;
                }else if(mAdapter.getItemViewType(position) == mAdapter.LOADING_ITEM){
                    return 2;
                }else{
                    return -1;
                }
            }
        });

        mService = CharactersApi.getApiService();

        //Только при первой странице т.к. при смене экрана снова вызывется данный метод из-за пересоздания активити
        if(PAGE.equals("1")){
            loadData();
        }
    }

    //Кладем следующую страницу, данные, состояние ресайлера.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.PAGE, PAGE);
        outState.putParcelableArrayList(Constants.DATA, (ArrayList<? extends Parcelable>) mAdapter.getGalleryItems());
        mRecyclerState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(Constants.RECYCLER_STATE, mRecyclerState);
        super.onSaveInstanceState(outState);
    }

    //отменяем вызов, чтобы не текло
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCall != null){
            mCall.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mRecyclerState != null){
            mLayoutManager.onRestoreInstanceState(mRecyclerState);
        }
    }

    /*
     Методы для работы с данными
   _________________________________________________________________________________________________
    */

    public void loadData(){
            mCall = mService.getCharactersJSON(PAGE);
            isLoading = true;
            mCall.enqueue(new Callback<Reponse>() {
                @Override
                public void onResponse(@NonNull Call<Reponse> call, @NonNull Response<Reponse> response) {
                    isLoading = false;
                    if(response.isSuccessful()){
                        PAGE = fetchPageNumber(response);
                        mAdapter.addAll(fetchResults(response));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Reponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
    }



    private List<Result> fetchResults(Response<Reponse> response){
        return response.body() != null ? response.body().getResults() : null;
    }

    //Из-за особенностей данного API разумнее брать номер следующей страницы из объекта INFO, тогда не нужно будет проверять общее число страниц и увеличивать текущую вручную
    //ИЗ-за особенностей Retrofit2 (нельзя менять baseURL) я достаю номер страницы regexp и отправляю в качестве параметра в запрос
    private String fetchPageNumber(Response<Reponse> response){
        String url = response.body() != null ? response.body().getInfo().getNext() : null;
        if(url != null && !url.isEmpty()){
             return url.replaceAll("\\D+","");
        }
        return null;
    }
}