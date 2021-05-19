create table t_idempotent
(
    id            bigint(11) auto_increment
        primary key,
    request_id   varchar(64)                          not null comment '请求编号',
    app_code varchar(64)                          not null comment '业务类型',
    request_desc       varchar(64)                           null comment '描述',
    business_no    varchar(255)                            null comment '业务编号',
    business_type  varchar(255)                          null comment '业务类型',
    business_desc    varchar(64)                           null comment '业务描述',
    status    tinyint(1)                           not null comment '状态0 进行中，1 成功，2 失败',
    yn            tinyint(1) default 1                 not null comment '数据有效性:0-无效,1-有效',
    created       datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    modified      datetime                             null
)
    comment '幂等表';

create table t_idempotent_body
(
    id            bigint(11) auto_increment
        primary key,
    idempotent_id   varchar(64)                          not null comment '幂等主键',
    request_param       text                           null comment '参数',
    response_body    blob                            null comment '结果',
    yn            tinyint(1) default 1                 not null comment '数据有效性:0-无效,1-有效',
    created       datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    modified      datetime                             null
)
    comment '幂等参数表';

