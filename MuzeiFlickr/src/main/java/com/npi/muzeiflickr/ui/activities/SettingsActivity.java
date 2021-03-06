package com.npi.muzeiflickr.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mobeta.android.dslv.DragSortListView;
import com.npi.muzeiflickr.BuildConfig;
import com.npi.muzeiflickr.R;
import com.npi.muzeiflickr.api.FlickrSource;
import com.npi.muzeiflickr.data.PreferenceKeys;
import com.npi.muzeiflickr.db.FGroup;
import com.npi.muzeiflickr.db.Photo;
import com.npi.muzeiflickr.db.RequestData;
import com.npi.muzeiflickr.db.Search;
import com.npi.muzeiflickr.db.Tag;
import com.npi.muzeiflickr.db.User;
import com.npi.muzeiflickr.network.FlickrApiData;
import com.npi.muzeiflickr.network.FlickrService;
import com.npi.muzeiflickr.network.FlickrServiceInterface;
import com.npi.muzeiflickr.ui.adapters.RequestAdapter;
import com.npi.muzeiflickr.ui.adapters.SourceSpinnerAdapter;
import com.npi.muzeiflickr.ui.dialogs.GroupChooserDialog;
import com.npi.muzeiflickr.ui.hhmmpicker.HHmsPickerBuilder;
import com.npi.muzeiflickr.ui.hhmmpicker.HHmsPickerDialogFragment;
import com.npi.muzeiflickr.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

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
 * Main settings activity
 */
public class SettingsActivity extends FragmentActivity implements HHmsPickerDialogFragment.HHmsPickerDialogHandler, GroupChooserDialog.ChooseGroupDialogListener {
    public static final String PREFS_NAME = "main_prefs";
    private static final String TAG = SettingsActivity.class.getSimpleName();
    public static final int GROUP_CHOSEN = 1;
    private TextView mRefreshRate;

    private DragSortListView mRequestList;
    private RequestAdapter mRequestAdapter;

    private RequestData mLastDeletedItem;
    private RelativeLayout mUndoContainer;
    private TextView mLastDeletedItemText;
    private DragSortListView.RemoveListener onRemove =
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {


                    if (BuildConfig.DEBUG) Log.d(TAG, "Removing item");

                    RequestData item = mRequestAdapter.getItem(which);
                    managePhotoFromSourceDeletion();
                    mRequestAdapter.remove(item);
                    if (item instanceof User) {
                        ((User) item).delete();
                    } else if (item instanceof Search) {
                        ((Search) item).delete();
                    } else if (item instanceof Tag) {
                        ((Tag) item).delete();
                    } else if (item instanceof FGroup) {
                        ((FGroup) item).delete();
                    }
                    mLastDeletedItem = item;
                    mRequestAdapter.notifyDataSetChanged();
                    mLastDeletedItemText.setText(item.getTitle());
                    mUndoContainer.setVisibility(View.VISIBLE);
                }
            };
    private UserInfoListener<FGroup> mCurrentGroupListener;
    private boolean mSourceAdded = false;

    private void managePhotoFromSourceDeletion() {
        if (mLastDeletedItem != null) {
            //Find all photos of the source
            Photo.deleteAll(Photo.class, "source_id = ? and source_type = ?", String.valueOf(mLastDeletedItem.getSourceId()), String.valueOf(mLastDeletedItem.getSourceType()));
        }
    }

    //Needed for Calligraphy
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault("");

        setContentView(R.layout.settings_activity);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final SharedPreferences.Editor editor = settings.edit();

        //Find views

        Switch wifiOnly = (Switch) findViewById(R.id.wifi_only);
        mRefreshRate = (TextView) findViewById(R.id.refresh_rate);
        ImageView aboutShortcut = (ImageView) findViewById(R.id.about);
        mRequestList = (DragSortListView) findViewById(R.id.content_list);
        mUndoContainer = (RelativeLayout) findViewById(R.id.undo_container);
        mLastDeletedItemText = (TextView) findViewById(R.id.last_deleted_item);
        TextView mLastDeletedUndo = (TextView) findViewById(R.id.last_deleted_undo);

        List<RequestData> items = new ArrayList<RequestData>();
        items.addAll(Search.listAll(Search.class));
        items.addAll(User.listAll(User.class));
        items.addAll(Tag.listAll(Tag.class));
        items.addAll(FGroup.listAll(FGroup.class));


        mRequestAdapter = new RequestAdapter(this, items);

        final View footerView = getLayoutInflater().inflate(R.layout.list_footer, null);
        mRequestList.addFooterView(footerView);

        mRequestList.setAdapter(mRequestAdapter);

        mRequestList.setRemoveListener(onRemove);

        populateFooter(footerView);

        //Wifi status and setting
        wifiOnly.setChecked(settings.getBoolean(PreferenceKeys.WIFI_ONLY, false));

        wifiOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean(PreferenceKeys.WIFI_ONLY, isChecked);
                editor.commit();
            }
        });


        //Other settings population
        int refreshRate = settings.getInt(PreferenceKeys.REFRESH_TIME, FlickrSource.DEFAULT_REFRESH_TIME);

        mRefreshRate.setText(Utils.convertDurationtoString(refreshRate));
        mRefreshRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HHmsPickerBuilder hpb = new HHmsPickerBuilder()
                        .setFragmentManager(getSupportFragmentManager())
                        .setStyleResId(R.style.MyCustomBetterPickerTheme);
                hpb.show();
            }
        });


        aboutShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutActivity.launchActivity(SettingsActivity.this);
            }
        });

        mLastDeletedUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastDeletedItem instanceof User) {
                    User user = ((User) mLastDeletedItem);
                    user.setId(null);
                    user.save();
                } else if (mLastDeletedItem instanceof Search) {
                    Search search = ((Search) mLastDeletedItem);
                    search.setId(null);
                    search.save();
                }
                mRequestAdapter.add(mLastDeletedItem);
                mRequestAdapter.notifyDataSetChanged();
                mUndoContainer.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        managePhotoFromSourceDeletion();

        //Launch an update if a source has been added
        if (mSourceAdded) {
            Intent intent = new Intent(FlickrSource.ACTION_RELOAD_SOME_PHOTOS);
            intent.setClass(this, FlickrSource.class);
            startService(intent);
        }
    }

    private void populateFooter(View footerView) {
        final View footerButton = footerView.findViewById(R.id.list_footer_button);
        final Spinner footerModeChooser = (Spinner) footerView.findViewById(R.id.mode_chooser);
        final RelativeLayout addItemContainer = (RelativeLayout) footerView.findViewById(R.id.new_item_container);
        final ImageButton footerSearchButton = (ImageButton) footerView.findViewById(R.id.footer_search_button);
        final ProgressBar footerProgress = (ProgressBar) footerView.findViewById(R.id.footer_progress);
        final EditText footerTerm = (EditText) footerView.findViewById(R.id.footer_term);

        footerButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int[] pos = new int[2];
                footerButton.getLocationInWindow(pos);

                String contentDesc = footerButton.getContentDescription().toString();
                Toast t = Toast.makeText(SettingsActivity.this, contentDesc, Toast.LENGTH_SHORT);
                t.show();
                t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, pos[1] + (footerButton.getHeight() / 2));

                return true;
            }
        });

        footerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemContainer.animate().alpha(1F);
                footerButton.animate().alpha(0F);
            }
        });


        //Mode spinner management
        ArrayAdapter<CharSequence> adapter = new SourceSpinnerAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.modes));


        footerModeChooser.setAdapter(adapter);

        footerSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchString = footerTerm.getText().toString();
                switch (footerModeChooser.getSelectedItemPosition()) {
                    case 0:

                        //It's a search

                        //Looking for a same existing search
                        List<Search> searchs = Search.listAll(Search.class);
                        for (Search search : searchs) {
                            if (search.getTitle().equals(searchString)) {
                                Toast.makeText(SettingsActivity.this, getString(R.string.search_exists), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        footerSearchButton.setVisibility(View.GONE);
                        footerProgress.setVisibility(View.VISIBLE);

                        getSearch(searchString, new UserInfoListener<Search>() {
                            @Override
                            public void onSuccess(Search search) {
                                mRequestAdapter.add(search);
                                mRequestAdapter.notifyDataSetChanged();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                                footerTerm.setText("");
                                footerModeChooser.setSelection(0);
                                addItemContainer.animate().alpha(0F);
                                footerButton.animate().alpha(1F);
                            }

                            @Override
                            public void onError(String reason) {
                                Toast.makeText(SettingsActivity.this, reason, Toast.LENGTH_LONG).show();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                            }
                        });

                        break;
                    case 1:
                        //It's an user

                        //Looking for a same existing search
                        List<User> users = User.listAll(User.class);
                        for (User user : users) {
                            if (user.getTitle().equals(searchString)) {
                                Toast.makeText(SettingsActivity.this, getString(R.string.user_exists), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        footerSearchButton.setVisibility(View.GONE);
                        footerProgress.setVisibility(View.VISIBLE);

                        getUserId(searchString, new UserInfoListener<User>() {
                            @Override
                            public void onSuccess(User user) {
                                mRequestAdapter.add(user);
                                mRequestAdapter.notifyDataSetChanged();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                                footerTerm.setText("");
                                footerModeChooser.setSelection(0);
                                addItemContainer.animate().alpha(0F);
                                footerButton.animate().alpha(1F);
                            }

                            @Override
                            public void onError(String reason) {
                                Toast.makeText(SettingsActivity.this, reason, Toast.LENGTH_LONG).show();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                            }
                        });
                        break;
                    case 2:
                        //It's a tag

                        //Looking for a same existing search
                        List<Tag> tags = Tag.listAll(Tag.class);
                        for (Tag tag : tags) {
                            if (tag.getTitle().equals(searchString)) {
                                Toast.makeText(SettingsActivity.this, getString(R.string.user_exists), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        footerSearchButton.setVisibility(View.GONE);
                        footerProgress.setVisibility(View.VISIBLE);

                        getTag(searchString, new UserInfoListener<Tag>() {
                            @Override
                            public void onSuccess(Tag tag) {
                                mRequestAdapter.add(tag);
                                mRequestAdapter.notifyDataSetChanged();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                                footerTerm.setText("");
                                footerModeChooser.setSelection(0);
                                addItemContainer.animate().alpha(0F);
                                footerButton.animate().alpha(1F);
                            }

                            @Override
                            public void onError(String reason) {
                                Toast.makeText(SettingsActivity.this, reason, Toast.LENGTH_LONG).show();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                            }
                        });
                        break;

                    case 3:
                        //It's an user

                        //Looking for a same existing search
                        List<FGroup> groups = FGroup.listAll(FGroup.class);
                        for (FGroup group : groups) {
                            if (group.getTitle().equals(searchString)) {
                                Toast.makeText(SettingsActivity.this, getString(R.string.group_exists), Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        footerSearchButton.setVisibility(View.GONE);
                        footerProgress.setVisibility(View.VISIBLE);

                        getGroupId(searchString, new UserInfoListener<FGroup>() {
                            @Override
                            public void onSuccess(FGroup group) {
                                mRequestAdapter.add(group);
                                mRequestAdapter.notifyDataSetChanged();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                                footerTerm.setText("");
                                footerModeChooser.setSelection(0);
                                addItemContainer.animate().alpha(0F);
                                footerButton.animate().alpha(1F);
                            }

                            @Override
                            public void onError(String reason) {
                                Toast.makeText(SettingsActivity.this, reason, Toast.LENGTH_LONG).show();
                                footerSearchButton.setVisibility(View.VISIBLE);
                                footerProgress.setVisibility(View.GONE);
                            }
                        });
                        break;
                }
            }
        });
    }

    private void getGroupId(final String search, final UserInfoListener<FGroup> userInfoListener) {
        FlickrService.getInstance().getGroups(search, new FlickrServiceInterface.IRequestListener<FlickrApiData.GroupsResponse>() {
            @Override
            public void onFailure() {
                userInfoListener.onError(getString(R.string.network_error));
            }

            @Override
            public void onSuccess(FlickrApiData.GroupsResponse photosResponse) {
                if (photosResponse == null || photosResponse.groups == null || photosResponse.groups.group == null) {
                    userInfoListener.onError(getString(R.string.network_error));
                } else if (photosResponse.groups.group.size() > 0) {
                    GroupChooserDialog.newInstance(new ArrayList<FlickrApiData.Group>(photosResponse.groups.group)).show(getFragmentManager(), "GroupChooserDialog");
                    mCurrentGroupListener = userInfoListener;
                } else {
                    userInfoListener.onError(getString(R.string.no_group_found));

                }
            }
        });
    }

    @Override
    public void onFinishChoosingDialog(final FlickrApiData.Group group) {

        //Get the photos number of the group
        FlickrService.getInstance().getGroupPhotos(group.nsid, 0, new FlickrServiceInterface.IRequestListener<FlickrApiData.PhotosResponse>() {
            @Override
            public void onFailure() {
                mCurrentGroupListener.onError(getString(R.string.group_no_photo));
            }

            @Override
            public void onSuccess(FlickrApiData.PhotosResponse photosResponse) {
                if (photosResponse == null || photosResponse.photos == null || photosResponse.photos.photo == null) {
                    mCurrentGroupListener.onError(getString(R.string.network_error));
                } else if (photosResponse.photos.photo.size() > 0) {
                    FGroup groupDB = new FGroup(SettingsActivity.this, group.nsid, group.name, 1, 0, photosResponse.photos.total);
                    groupDB.save();
                    mSourceAdded = true;
                    mCurrentGroupListener.onSuccess(groupDB);
                } else {
                    mCurrentGroupListener.onError(getString(R.string.group_no_photo));

                }
            }
        });


    }


    private void getSearch(final String search, final UserInfoListener<Search> userInfoListener) {


        FlickrService.getInstance().getPopularPhotos(search, 0, new FlickrServiceInterface.IRequestListener<FlickrApiData.PhotosResponse>() {
            @Override
            public void onFailure() {
                userInfoListener.onError(getString(R.string.network_error));
            }

            @Override
            public void onSuccess(FlickrApiData.PhotosResponse photosResponse) {
                if (photosResponse == null || photosResponse.photos == null || photosResponse.photos.photo == null) {
                    userInfoListener.onError(getString(R.string.network_error));
                } else if (photosResponse.photos.photo.size() > 0) {
                    Search searchDB = new Search(SettingsActivity.this, search, 1, 0, photosResponse.photos.total);
                    searchDB.save();
                    mSourceAdded = true;
                    userInfoListener.onSuccess(searchDB);
                } else {
                    userInfoListener.onError(getString(R.string.user_no_photo));

                }
            }
        });


    }

    private void getTag(final String search, final UserInfoListener<Tag> userInfoListener) {


        FlickrService.getInstance().getPopularPhotosByTag(search, 0, new FlickrServiceInterface.IRequestListener<FlickrApiData.PhotosResponse>() {
            @Override
            public void onFailure() {
                userInfoListener.onError(getString(R.string.network_error));
            }

            @Override
            public void onSuccess(FlickrApiData.PhotosResponse photosResponse) {
                if (photosResponse == null || photosResponse.photos == null || photosResponse.photos.photo == null) {
                    userInfoListener.onError(getString(R.string.network_error));
                } else if (photosResponse.photos.photo.size() > 0) {
                    Tag tagDB = new Tag(SettingsActivity.this, search, 1, 0, photosResponse.photos.total);
                    tagDB.save();
                    mSourceAdded = true;
                    userInfoListener.onSuccess(tagDB);
                } else {
                    userInfoListener.onError(getString(R.string.tag_no_photo));

                }
            }
        });


    }


    /**
     * The duration picker has been closed
     *
     * @param reference
     * @param hours
     * @param minutes
     * @param seconds
     */
    @Override
    public void onDialogHmsSet(int reference, int hours, int minutes, int seconds) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        int duration = hours * 3600000 + minutes * 60000 + seconds * 1000;
        editor.putInt(PreferenceKeys.REFRESH_TIME, duration);
        editor.commit();
        mRefreshRate.setText(Utils.convertDurationtoString(duration));


    }

    /**
     * Determine if a user exists
     *
     * @param user the user to search
     */
    private void getUserId(final String user, final UserInfoListener userInfoListener) {

        FlickrService.getInstance().getUserByName(user, new FlickrServiceInterface.IRequestListener<FlickrApiData.UserByNameResponse>() {
            @Override
            public void onFailure() {
                userInfoListener.onError(getString(R.string.network_error));

            }

            @Override
            public void onSuccess(FlickrApiData.UserByNameResponse userByNameResponse) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Looking for user");


                //The user has not been found
                if (userByNameResponse == null || userByNameResponse.user == null || userByNameResponse.user.nsid == null) {
                    if (BuildConfig.DEBUG) Log.d(TAG, "User not found");
                    userInfoListener.onError(getString(R.string.user_not_found));

                    return;
                }


                //The user has been found, we store it
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "User found: " + userByNameResponse.user.nsid + " for " + user);
                }
                final String userId = userByNameResponse.user.nsid;


                //User has been found, let's see if he has photos

                FlickrService.getInstance().getPopularPhotosByUser(userId, 0, new FlickrServiceInterface.IRequestListener<FlickrApiData.PhotosResponse>() {
                    @Override
                    public void onFailure() {
                        userInfoListener.onError(getString(R.string.user_no_photo));
                    }

                    @Override
                    public void onSuccess(FlickrApiData.PhotosResponse photosResponse) {
                        if (photosResponse.photos.photo.size() > 0) {
                            User userDB = new User(SettingsActivity.this, userId, user, 1, 0, photosResponse.photos.total);
                            userDB.save();
                            mSourceAdded = true;
                            userInfoListener.onSuccess(userDB);
                        } else {
                            userInfoListener.onError(getString(R.string.user_no_photo));

                        }
                    }
                });
            }
        });


    }


    public interface UserInfoListener<T> {
        void onSuccess(T user);

        void onError(String reason);
    }


}
