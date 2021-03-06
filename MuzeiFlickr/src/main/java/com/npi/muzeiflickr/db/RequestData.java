package com.npi.muzeiflickr.db;

/**
 * Created by nicolas on 17/02/14.
 */
public interface RequestData {

    String getTitle();

    int getIconRessource();

    String getPhotoTotal();

    String getCurrentPhotoIndex();

    int getCurrentPage();

    void setPage(int i);

    int getSourceType();

    long getSourceId();

    void incrementCurrent();
}
