package me.zsj.interessant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.zsj.interessant.api.SearchApi;
import me.zsj.interessant.base.ToolbarActivity;
import me.zsj.interessant.interesting.InterestingAdapter;
import me.zsj.interessant.model.ItemList;
import me.zsj.interessant.rx.ErrorAction;
import me.zsj.interessant.rx.RxScroller;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author zsj
 */

public class ResultActivity extends ToolbarActivity {

    private int start;
    private List<ItemList> itemLists = new ArrayList<>();
    private SearchApi searchApi;

    private InterestingAdapter adapter;


    @Override
    public int providerLayoutId() {
        return R.layout.search_result_activity;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        final String keyword = getIntent().getStringExtra(SearchActivity.KEYWORD);

        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle(keyword);

        searchApi = InteressantFactory.getRetrofit().createApi(SearchApi.class);

        adapter = new InterestingAdapter(this, itemLists);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        fetchResult(keyword);

        RxRecyclerView.scrollStateChanges(recyclerView)
                .compose(bindToLifecycle())
                .compose(RxScroller.scrollTransformer(layoutManager,
                        adapter, itemLists))
                .subscribe(newState -> {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        start += 10;
                        fetchResult(keyword);
                    }
                });
    }

    private void fetchResult(final String keyword) {
        searchApi.query(keyword, start)
                .compose(bindToLifecycle())
                .filter(searchResult -> searchResult != null)
                .map(searchResult -> searchResult.itemList)
                .doOnNext(itemList -> itemLists.addAll(itemList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(itemLists1 -> {
                    adapter.notifyDataSetChanged();
                }, ErrorAction.errorAction(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.search_action) {
            toSearch(this);
        }
        return super.onOptionsItemSelected(item);
    }
}
