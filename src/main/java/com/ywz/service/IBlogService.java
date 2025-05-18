package com.ywz.service;

import com.ywz.dto.Result;
import com.ywz.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    Result queryHotBlog(Integer current);

    Result saveBlog(Blog blog);

    Result getBlog(Integer id);

    Result likeBlog(Long id);

    Result queryLikes(Integer id);

    Result queryBlogForFollow(Long max, Integer offset);
}
