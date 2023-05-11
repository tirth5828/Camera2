package com.example.camera2;

import android.graphics.Bitmap;

public class image {
    int superindex;

    int subindex;

    Bitmap bitmap;

    public image(Bitmap bitmap,int superindex,int subindex){
        this.bitmap = bitmap;
        this.superindex = superindex;
        this.subindex = subindex;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getSubindex() {
        return subindex;
    }

    public int getSuperindex() {
        return superindex;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setSubindex(int subindex) {
        this.subindex = subindex;
    }

    public void setSuperindex(int superindex) {
        this.superindex = superindex;
    }
}
