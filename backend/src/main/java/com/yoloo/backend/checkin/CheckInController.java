package com.yoloo.backend.checkin;

import com.google.appengine.api.users.User;
import com.yoloo.backend.base.Controller;
import com.yoloo.backend.util.StringUtil;

import java.util.List;

import javax.annotation.Nonnull;

import ix.Ix;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

@Log
@AllArgsConstructor(staticName = "create")
public class CheckInController extends Controller {

  public CheckIn insertCheckIn(@Nonnull String location, User user) {
    List<Float> points = Ix.from(StringUtil.split(location, ","))
        .map(Float::parseFloat)
        .toList();
    // TODO: 18.03.2017 Finish checkin
    return null;
  }
}
