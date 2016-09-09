package com.coltan.keralaattractions;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Robo on 25-08-2016.
 */

public class Photo implements Parcelable {

    //Author info
    private String authorName;
    private String authorId;
    private String authorPhotoUrl;

    //Photo info
    private String photo;
    private String title;
    private String description;
    private String place;
    private String latitude;
    private String longitude;
    private String date;
    private String tags;

    public Photo() {
    }

    public Photo(String author, String authorId, String authorPhotoUrl, String title, String place,
                 String description, String photo) {
        this.authorName = author;
        this.authorId = authorId;
        this.authorPhotoUrl = authorPhotoUrl;
        this.title = title;
        this.place = place;
        this.description = description;
        this.photo = photo;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorPhotoUrl() {
        return authorPhotoUrl;
    }

    public void setAuthorPhotoUrl(String authorPhotoUrl) {
        this.authorPhotoUrl = authorPhotoUrl;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authorName);
        dest.writeString(authorId);
        dest.writeString(authorPhotoUrl);
        dest.writeString(title);
        dest.writeString(place);
        dest.writeString(description);
        dest.writeString(photo);

    }

    protected Photo(Parcel in) {
        authorName = in.readString();
        authorId = in.readString();
        authorPhotoUrl = in.readString();
        title = in.readString();
        place = in.readString();
        description = in.readString();
        photo = in.readString();
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
}
