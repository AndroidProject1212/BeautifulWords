package com.otb.designerassist.mvp.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apkfuns.logutils.LogUtils;
import com.lnyp.flexibledivider.HorizontalDividerItemDecoration;
import com.lnyp.recyclerview.EndlessRecyclerOnScrollListener;
import com.lnyp.recyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.lnyp.recyclerview.RecyclerViewLoadingFooter;
import com.lnyp.recyclerview.RecyclerViewStateUtils;
import com.otb.designerassist.R;
import com.otb.designerassist.mvp.model.entity.SentenceCollection;
import com.otb.designerassist.mvp.presenter.impl.AlbumsPresenter;
import com.otb.designerassist.mvp.ui.adapter.JujiAdapter;
import com.otb.designerassist.mvp.ui.view.IAlbumsView;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.List;

public class FragmentJujiList extends Fragment implements IAlbumsView {

    private static final String ARG_TYPE = "type";

    public RecyclerView listJuzi;

    public RotateLoading rotateloading;

    private String type;

    private AlbumsPresenter albumsPresenter;

    private View view;

    private List<SentenceCollection> mDatas;

    private HeaderAndFooterRecyclerViewAdapter mAdapter;

    private String page;

    private boolean mHasMore = true;

    public FragmentJujiList() {

    }

    public static FragmentJujiList newInstance(String type) {

        FragmentJujiList fragment = new FragmentJujiList();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_juji_list, container, false);
        }

        initView();

        albumsPresenter = new AlbumsPresenter(this);

        rotateloading.start();
        qryMeijus();

        return view;
    }

    private void initView() {

        rotateloading = (RotateLoading) view.findViewById(R.id.rotateloading);
        listJuzi = (RecyclerView) view.findViewById(R.id.listJuzi);

        mDatas = new ArrayList<>();

        JujiAdapter jujiAdapter = new JujiAdapter(this, mDatas, onClickListener);
        mAdapter = new HeaderAndFooterRecyclerViewAdapter(jujiAdapter);
        listJuzi.setAdapter(mAdapter);

        listJuzi.setLayoutManager(new LinearLayoutManager(getActivity()));
        listJuzi.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .colorResId(R.color.divider_color)
//                        .size(20)
                        .build());

        listJuzi.addOnScrollListener(mOnScrollListener);
    }

    private EndlessRecyclerOnScrollListener mOnScrollListener = new EndlessRecyclerOnScrollListener() {
        @Override
        public void onLoadNextPage(View view) {
            super.onLoadNextPage(view);

            RecyclerViewLoadingFooter.State state = RecyclerViewStateUtils.getFooterViewState(listJuzi);

            if (state == RecyclerViewLoadingFooter.State.Loading) {
                return;
            }

            if (mHasMore) {
                RecyclerViewStateUtils.setFooterViewState(getActivity(), listJuzi, mHasMore, RecyclerViewLoadingFooter.State.Loading, null);
                qryMeijus();

            } else {
                RecyclerViewStateUtils.setFooterViewState(getActivity(), listJuzi, mHasMore, RecyclerViewLoadingFooter.State.TheEnd, null);
            }
        }
    };

    private View.OnClickListener mFooterClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            RecyclerViewStateUtils.setFooterViewState(getActivity(), listJuzi, mHasMore, RecyclerViewLoadingFooter.State.Loading, null);

            qryMeijus();
        }
    };

    private void qryMeijus() {
        albumsPresenter.loadAlbums(getActivity(), type, page);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (null != view) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }


    @Override
    public void onSuccess(List<SentenceCollection> sentenceCollections) {
        if (page == null) {
            page = "1";
        } else {
            int i_page = Integer.parseInt(page);
            i_page = i_page + 1;
            page = "" + i_page;
        }

        if (sentenceCollections != null) {
            LogUtils.e("page : " + page + " size : " + sentenceCollections.size());
            mDatas.addAll(sentenceCollections);
            mAdapter.notifyDataSetChanged();
        }

        rotateloading.stop();

        RecyclerViewStateUtils.setFooterViewState(listJuzi, RecyclerViewLoadingFooter.State.Normal);
    }

    @Override
    public void onError(Throwable e) {
        RecyclerViewStateUtils.setFooterViewState(getActivity(), listJuzi, mHasMore, RecyclerViewLoadingFooter.State.NetWorkError, mFooterClick);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
}