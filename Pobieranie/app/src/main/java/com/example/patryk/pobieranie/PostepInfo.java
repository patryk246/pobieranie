package com.example.patryk.pobieranie;

import android.os.Parcel;
import android.os.Parcelable;

public class PostepInfo implements Parcelable {
        public long mPobranychBajtow;
        public long mRozmiar;
        public String mWynik;
        public PostepInfo() {
            mPobranychBajtow=0;
            mRozmiar=0;
        }
        // obowiązkowy konstruktor tworzy obiekt na podstawie paczki
        public PostepInfo(Parcel paczka) {
           mRozmiar=paczka.readLong();
           mPobranychBajtow=paczka.readLong();
           mWynik=paczka.readString();
        }
        @Override
        public int describeContents() {
            return 0;
        }
        // zapisuje do obiekt do paczki
        @Override
        public void writeToParcel(Parcel dest, int flags) {


           dest.writeLong(mRozmiar);
            dest.writeLong(mPobranychBajtow);
            dest.writeString(mWynik);
        }
        // trzeba utworzyć obiekt CREATOR
        public static final Parcelable.Creator<PostepInfo> CREATOR =
                new Parcelable.Creator<PostepInfo>() {
                    @Override
                    public PostepInfo createFromParcel(Parcel source) {
                        return new PostepInfo(source);
                    }
                    @Override
                    public PostepInfo[] newArray(int size) {
                        return new PostepInfo[size];
                    }
                };
    }

