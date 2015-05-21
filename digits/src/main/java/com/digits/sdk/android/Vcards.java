package com.digits.sdk.android;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

class Vcards {
    @SerializedName("vcards")
    final List<String> vcards = new ArrayList<>();

    Vcards(List<String> vCards) {
        this.vcards.addAll(vCards);
    }
}
