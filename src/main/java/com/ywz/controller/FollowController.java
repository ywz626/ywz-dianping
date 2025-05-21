package com.ywz.controller;


import com.ywz.dto.Result;
import com.ywz.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long id, @PathVariable("isFollow") Boolean isFollow) {
        return followService.follow(id,isFollow);
    }

    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long id) {
        return followService.isFollow(id);
    }
    @GetMapping("/common/{id}")
    public Result commonFollows(@PathVariable("id") Long id) {
        return followService.commonFollows(id);
    }
}
