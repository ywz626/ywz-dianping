package com.ywz.utils;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/14 15:56
 */
public interface ILock {
    boolean tryLock(Long timeOut);

    boolean unLock();
}
