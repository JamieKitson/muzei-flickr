/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Created by nicolas on 14/02/14.
 * Retrofit service describing requests
 */

package com.npi.muzeiflickr.network;

import com.npi.muzeiflickr.utils.Config;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface FlickrServiceInterface {

    @GET("/?&method=flickr.people.getPhotos&per_page=5&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getPopularPhotosByUser(@Query("privacy_filter") String privacy, @Query("user_id") String userId, @Query("page") int page, Callback<FlickrApiData.PhotosResponse> callback);

    @GET("/?&method=flickr.people.getInfo&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getUser(@Query("user_id") String owner, Callback<FlickrApiData.UserResponse> callback);

    @GET("/?&method=flickr.photos.search&per_page=5&safe_search=1&sort=interestingness-desc&format=json&license=1,2,3,4,5,6,7&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getPopularPhotos(@Query("privacy_filter") String privacy, @Query("text") String text, @Query("page") int page, Callback<FlickrApiData.PhotosResponse> callback);

    @GET("/?&method=flickr.photos.search&per_page=5&safe_search=1&sort=interestingness-desc&format=json&license=1,2,3,4,5,6,7&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getPopularPhotosByTag(@Query("privacy_filter") String privacy, @Query("tags") String text, @Query("page") int page, Callback<FlickrApiData.PhotosResponse> callback);

    @GET("/?&method=flickr.groups.search&format=json&per_page=50&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getGroups(@Query("text") String text, Callback<FlickrApiData.GroupsResponse> callback);

    @GET("/?&method=flickr.groups.pools.getPhotos&format=json&per_page=5&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getGroupPhotos(@Query("group_id") String groupId, @Query("page") int page, Callback<FlickrApiData.PhotosResponse> callback);

    @GET("/?&method=flickr.photosets.getPhotos&format=json&per_page=5&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getSetPhotos(@Query("photoset_id") String setId, @Query("page") int page, Callback<FlickrApiData.SetPhotosResponse> callback);


    @GET("/?&method=flickr.photos.getSizes&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getSize(@Query("photo_id") String photo, Callback<FlickrApiData.SizeResponse> callback);

    @GET("/?&method=flickr.people.findByUsername&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getUserByName(@Query("username") String user, Callback<FlickrApiData.UserByNameResponse> callback);

    @GET("/?&method=flickr.test.login&format=json&oauth_consumer_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getLogin(Callback<FlickrApiData.UserLoginResponse> callback);

    @GET("/?&method=flickr.contacts.getList&format=json&oauth_consumer_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getContacts(Callback<FlickrApiData.ContactResponse> callback);

    @GET("/?&method=flickr.people.getGroups&format=json&oauth_consumer_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getGroupsByUser(@Query("user_id") String nsid, Callback<FlickrApiData.GroupsResponse> callback);

    @GET("/?&method=flickr.photos.getInfo&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getPhotoInfo(@Query("photo_id") String photoId, Callback<FlickrApiData.PhotoResponse> callback);

    @GET("/?&method=flickr.favorites.getList&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getFavorites(Callback<FlickrApiData.PhotosResponse> callback);

    @POST("/?&method=flickr.favorites.add&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void addFavorite(@Query("photo_id") String photoId, Callback<FlickrApiData.AddFavoriteResponse> callback);

    @POST("/?&method=flickr.urls.getGroup&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getGroupUrl(@Query("group_id") String groupId, Callback<FlickrApiData.GroupUrlResponse> callback);

    @POST("/?&method=flickr.interestingness.getList&per_page=5&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void getInterrestingness(@Query("page") String page, Callback<FlickrApiData.PhotosResponse> callback);

    @POST("/?&method=flickr.photosets.getList&format=json&api_key=" + Config.CONSUMER_KEY + "&nojsoncallback=1")
    void findSetByUser(@Query("user_id") String userId, Callback<FlickrApiData.PhotosSetsResponse> callback);

    public interface IRequestListener<T> {
        void onFailure();

        void onSuccess(T response);
    }
}
