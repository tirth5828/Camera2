package com.example.camera2;

import java.util.ArrayList;

public class imageArray {
    ArrayList<image> imageArrayList;

    public imageArray(){
        imageArrayList = new ArrayList<>();
    }

    public ArrayList<image> getImageArrayList() {
        return imageArrayList;
    }

    public void setImageArrayList(ArrayList<image> imageArrayList) {
        this.imageArrayList = imageArrayList;
    }

    public void addImage(image image){
        imageArrayList.add(image);
    }

    public int getLength(){
        return imageArrayList.size();
    }
}
