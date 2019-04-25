package com.example.popularnews;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popularnews.api.ApiClient;
import com.example.popularnews.api.ApiInterface;
import com.example.popularnews.models.Article;
import com.example.popularnews.models.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final String API_KEY = "d8ccd6ce129749f8beec32bde14c3e5f";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;
    private String TAG = MainActivity.class.getSimpleName();
    private TextView topHeadline;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadline = findViewById(R.id.topheadline);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        onLoadingSwipeRefresh("");

    }

    public void LoadJson(final String keyword){

        topHeadline.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        String coutry = Utils.getCountry();

        Call<News> call ;

        if (keyword.length() > 0){
            call = apiInterface.getNewsSearch(keyword, coutry, "publishedAt", API_KEY);
        }else {
            call = apiInterface.getNews(coutry,API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null){

                    if (!articles.isEmpty()){
                        articles.clear();
                    }

                    articles = response.body().getArticle();
                    adapter = new Adapter(articles, MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListner();

                    topHeadline.setVisibility(View.VISIBLE);
                    swipeRefreshLayout.setRefreshing(false);


                }else {
                    topHeadline.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                topHeadline.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }


    private void initListner(){
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, NewsDetailActivity.class);

                Article article = articles.get(position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("source", article.getSource().getName());
                intent.putExtra("author", article.getAuthor());

                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2){
                    onLoadingSwipeRefresh(query);
                }else {
                    Toast.makeText(MainActivity.this, "Type more than two letters", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false,false);

        return true;
    }

    @Override
    public void onRefresh() {
        LoadJson("");
    }

    private void onLoadingSwipeRefresh(final String keyword){

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                LoadJson(keyword);
            }
        }
        );
    }
}
