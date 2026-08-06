package com.bookmatch.BookMatch.client.googlebooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleBookItem {

    private GoogleBookVolumeInfo volumeInfo;
}