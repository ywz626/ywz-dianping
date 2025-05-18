package com.ywz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ywz.dto.Result;
import com.ywz.dto.UserDTO;
import com.ywz.entity.Follow;
import com.ywz.entity.User;
import com.ywz.mapper.FollowMapper;
import com.ywz.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ywz.service.IUserService;
import com.ywz.utils.RedisConstants;
import com.ywz.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private FollowMapper followMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    @Override
    public Result follow(Long id, Boolean isFollow) {
        Long uid = UserHolder.getUser().getId();
        Follow follow = new Follow();
        if(isFollow){
            // 关注
            follow.setFollowUserId(id);
            follow.setUserId(uid);
            boolean success = save(follow);
            if(success) {
                stringRedisTemplate.opsForSet().add(RedisConstants.USER_FOLLOW_KEY + uid, id.toString());
            }
        }else {
            // 取消关注
            boolean success = remove(new QueryWrapper<Follow>().eq("user_id", uid).eq("follow_user_id", id));
            if(success){
                stringRedisTemplate.opsForSet().remove(RedisConstants.USER_FOLLOW_KEY+uid,id.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long id) {
        Long uid = UserHolder.getUser().getId();
        Boolean success = stringRedisTemplate.opsForSet().isMember(RedisConstants.USER_FOLLOW_KEY + uid, id.toString());
        return Result.ok(success);
    }

    @Override
    public Result commonFollows(Long id) {
        Set<String> set = stringRedisTemplate.opsForSet().intersect(RedisConstants.USER_FOLLOW_KEY + id, RedisConstants.USER_FOLLOW_KEY + UserHolder.getUser().getId());
        List<UserDTO> common = null;
        if (set != null) {
            common = new ArrayList<>(set)
                    .stream()
                    .map(Long::valueOf)
                    .map(aLong -> {
                        User user = userService.getById(aLong);
                        return BeanUtil.copyProperties(user, UserDTO.class);
                    }).collect(Collectors.toList());
        }
        return Result.ok(common);
    }
}
