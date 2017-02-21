package com.yoloo.backend.mapper;

public interface Mapper<From, To> {

  To map(From from);
}
