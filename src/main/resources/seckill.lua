-- 由lua脚本来完成扣减库存防止超卖，一人一单功能

local voucherId = ARGV[1]
local userId = ARGV[2]

-- 获取 库存key
local stockId = "seckill:stock:" .. voucherId
-- 获取 订单key
local orderId = "seckill:order:" .. voucherId

if (tonumber(redis.call('get',stockId)) <= 0) then
    return 1
end

if (redis.call('sismember',orderId,userId) == 1) then
    return 2
end

--redis.call('incrby',stockId,-1)
--redis.call('sadd',orderId,userId)
return 0

