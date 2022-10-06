package at.gastronaut.android.classes;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class Sponsor {

    private String filename;
    private String text;
    private int id;

    private Bitmap bmp = null;

    public Sponsor(int id, String filename, Bitmap src, String text) {
        this.id = id;
        this.filename = filename;
        this.bmp = src;
        this.text = text;
    }

    public Sponsor(int id, String filename, Bitmap src) {
        this(id, filename, src, "");
    }

    public void setBitmap(Bitmap b) {
        this.bmp = b;
    }

    public int getId() {
        return this.id;
    }

    public Bitmap getBitmap() {
        return this.bmp;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || !this.filename.equals(((Sponsor) obj).filename)) {
            return false;
        }

        return false;
    }
}
