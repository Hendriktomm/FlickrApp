package com.example.het.photogallery;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by HET on 25.7.2014.
 */
public class PhotoGalleryFragment extends Fragment{
    private static final String TAG = "PhotoGalleryFragment";
    GridView mGridView;

    ArrayList<GalleryItem> mItems;

    ThumbnailDownloader<ImageView> mThumbnailThread;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchItemsTask().execute();

        mThumbnailThread = new ThumbnailDownloader(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
                                         public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail){
                                             if(isVisible()){
                                                 imageView.setImageBitmap(thumbnail);
                                             }
                                         }
                                     });
            mThumbnailThread.start();
            mThumbnailThread.getLooper();
            Log.i(TAG,"Background thread started");
        }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

        @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mGridView = (GridView)v.findViewById(R.id.gridView);

        setupAdapter();
        return v;

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    void setupAdapter(){
        if(getActivity() == null || mGridView == null) return;
        if(mItems != null){
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        }
        else{
            mGridView.setAdapter(null);
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params){
            return new FlickrFetchr().fetchItems();

        }
        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items){
            mItems = items;
            setupAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(),0,items);

        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item,parent, false);
            }

            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.mynd);

            GalleryItem item = getItem(position);
            mThumbnailThread.queueThumbnail(imageView,item.getmUrl());
            return convertView;

        }

    }


}
