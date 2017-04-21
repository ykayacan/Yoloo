package com.yoloo.backend.group;

import com.googlecode.objectify.Key;
import com.yoloo.backend.travelertype.TravelerTypeEntity;
import ix.Ix;
import java.util.List;
import javax.annotation.Nonnull;

import static com.yoloo.backend.OfyService.ofy;

public class TravelerGroupService {

  public List<Key<TravelerGroupEntity>> findGroupKeys(@Nonnull List<String> travelerTypeIds) {
    return Ix
        .from(travelerTypeIds)
        .map(Key::<TravelerTypeEntity>create)
        .collectToList()
        .flatMap(keys -> ofy().load().keys(keys).values())
        .map(TravelerTypeEntity::getGroupKeys)
        .flatMap(Ix::from)
        .distinct()
        .toList();
  }
}
