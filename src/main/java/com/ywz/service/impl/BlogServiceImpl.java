package com.ywz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ywz.dto.Result;
import com.ywz.dto.ScrollResult;
import com.ywz.dto.UserDTO;
import com.ywz.entity.Blog;
import com.ywz.entity.Follow;
import com.ywz.entity.User;
import com.ywz.mapper.BlogMapper;
import com.ywz.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.service.IFollowService;
import com.ywz.service.IUserService;
import com.ywz.utils.RedisConstants;
import com.ywz.utils.SystemConstants;
import com.ywz.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(this::queryUser);
        return Result.ok(records);
    }

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.ok();
        }
        // 获取该用户所有粉丝
        List<Follow> funs = followService.query().eq("follow_user_id", user.getId()).list();
        for (Follow follow : funs) {
            Long userId = follow.getUserId();
            stringRedisTemplate.opsForZSet().add("feed:" + userId,blog.getId().toString(),System.currentTimeMillis());
        }
        return Result.ok(blog.getId());
    }

    @Override
    public Result getBlog(Integer id) {
        Blog blog = getById(id);
        if (blog == null) {
            return Result.fail("用户不存在");
        }
        queryUser(blog);
        return Result.ok(blog);
    }

    @Override
    public Result likeBlog(Long id) {
        // 使用zset类型重构
        Long userId = UserHolder.getUser().getId();
        Double score = stringRedisTemplate.opsForZSet().score(RedisConstants.BLOG_LIKED_KEY + id, userId.toString());
        if (score == null) {
            // 没有点过赞
            boolean isSuccess = update().setSql("liked = liked + 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(RedisConstants.BLOG_LIKED_KEY + id, userId.toString(), System.currentTimeMillis());
                return Result.ok();
            }
        }
        else {
            boolean isSuccess = update().setSql("liked = liked - 1")
                    .eq("id", id)
                    .update();
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(RedisConstants.BLOG_LIKED_KEY + id, userId.toString());
                return Result.ok();
            }
        }
        return Result.ok();

//         使用 set类型，但无法完成点赞列表功能
//        Long uid = UserHolder.getUser().getId();
//        String key = RedisConstants.BLOG_LIKED_KEY + id;
//        Boolean exist = stringRedisTemplate.opsForSet().isMember(key, uid.toString());
//        // 如果 exist为true代表已点赞
//        Blog blog = getById(id);
//        if (Boolean.FALSE.equals(exist)) {
//            boolean isSuccess = update().setSql("liked = liked + 1")
//                    .eq("id", id)
//                    .update();
//            if (isSuccess) {
//                stringRedisTemplate.opsForSet().add(key, uid.toString());
//
//                blog.setIsLike(true);
//                return Result.ok(blog);
//            }
//        }
//        boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
//        if (isSuccess) {
//            stringRedisTemplate.opsForSet().remove(key, uid.toString());
//            blog.setIsLike(false);
//        }
//        return Result.ok(blog);
    }

    @Override
    public Result queryLikes(Integer id) {
        // 使用redis的zset类型
        Set<String> uidSet = stringRedisTemplate.opsForZSet().range(RedisConstants.BLOG_LIKED_KEY + id, 0, 4);
        if(uidSet == null || uidSet.size() == 0){
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = uidSet.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        String join = StrUtil.join(",", ids);
        List<UserDTO> users = userService.query().in("id",ids).last("order by field(id," + join + ")")
                .list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class)).collect(Collectors.toList());
        return Result.ok(users);
    }

    @Override
    public Result queryBlogForFollow(Long max, Integer offset) {
        // 查询用户
        Long uid = UserHolder.getUser().getId();
        // 查询收件箱
        String key = RedisConstants.FEED_KEY + uid;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, max, offset, 3);
        // 解析 blogid ，mintime ，offset
        ArrayList<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0L;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            ids.add(Long.valueOf(typedTuple.getValue()));
            long time = typedTuple.getScore().longValue();
            if(time == minTime){
                os ++;
            } else {
                minTime = time;
                os =1;
            }
        }
        String join = StrUtil.join(",", ids);
        List<Blog> blogs = query().in("id", ids).last("order by field(id," + join + ")").list();
        blogs.forEach(this::queryUser);
        ScrollResult result = ScrollResult.builder()
                .list(blogs)
                .minTime(minTime)
                .offset(os)
                .build();
        // 返回数据
        return Result.ok(result);
    }

    private void queryUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        isBlogLiked(blog);
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if (user == null) {
            return;
        }
        Double score = stringRedisTemplate.opsForZSet().score(RedisConstants.BLOG_LIKED_KEY + blog.getId(), UserHolder.getUser().getId().toString());
        blog.setIsLike(score != null);
    }
}
