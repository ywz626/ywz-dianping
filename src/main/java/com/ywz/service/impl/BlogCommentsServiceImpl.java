package com.ywz.service.impl;

import com.ywz.entity.BlogComments;
import com.ywz.mapper.BlogCommentsMapper;
import com.ywz.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
