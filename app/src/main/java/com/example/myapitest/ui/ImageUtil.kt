package com.example.myapitest.ui

import com.example.myapitest.R
import com.squareup.picasso.Picasso
import android.widget.ImageView

fun ImageView.loadUrl(imageUrl: String) {
    Picasso.get()
        .load(imageUrl)
        .placeholder(R.drawable.ic_download)
        .error(R.drawable.ic_error)
        .transform(CircleTransform())
        .into(this)
}

