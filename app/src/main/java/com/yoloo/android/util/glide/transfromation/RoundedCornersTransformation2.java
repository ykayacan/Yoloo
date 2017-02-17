package com.yoloo.android.util.glide.transfromation;

/*
  Copyright (C) 2015 Wasabeef

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

public class RoundedCornersTransformation2 implements Transformation<Bitmap> {

  private final Context context;
  private final BitmapPool bitmapPool;
  private final float radius;

  public RoundedCornersTransformation2(Context context, float radius) {
    this.context = context;
    this.bitmapPool = Glide.get(context).getBitmapPool();
    this.radius = radius;
  }

  @Override
  public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
    Bitmap source = resource.get();

    RoundedBitmapDrawable drawable =
        RoundedBitmapDrawableFactory.create(context.getResources(), source);
    drawable.setCornerRadius(radius);

    return BitmapResource.obtain(drawable.getBitmap(), bitmapPool);
  }

  @Override public String getId() {
    return RoundedCornersTransformation2.class.getName();
  }
}
