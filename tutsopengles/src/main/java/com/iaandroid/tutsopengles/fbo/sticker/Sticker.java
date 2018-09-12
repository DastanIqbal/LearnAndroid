package com.iaandroid.tutsopengles.fbo.sticker;

import com.dastanapps.mediasdk.opengles.gpu.fbo.Component;

import java.io.Serializable;
import java.util.List;

/**
 * 贴纸模型
 *
 * @author like
 * @date 2018-01-05
 */
public class Sticker implements Serializable {

    private static final long serialVersionUID = 1L;

    // 组件列表
    public List<Component> components;
}
