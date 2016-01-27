INSERT IGNORE INTO `users` (
`id`, `externalId`, `username`,
`formattedName`, `familyName`, `givenName`,  `nickname`, 
`profileURL`, `active`, `password`, `displayName`
)
VALUES
(
'2819c223-7f76-453a-919d-413861904646', 'soleo', 'soleoshao',
'Xinjiang Shao', 'Shao', 'Xinjiang',  'soleo',
'http://xinjiangshao.com', true, '123456', 'Xinjiang Shao'
);

INSERT IGNORE INTO `emails` (
    `userId`, `value`, `type`, `isPrimary`
)VALUES
('2819c223-7f76-453a-919d-413861904646', 'shaoxinjiang@gmail.com' ,'work', true),
('2819c223-7f76-453a-919d-413861904646', 'xinjiang.shao@gmail.com' ,'personal', false)
;

INSERT IGNORE INTO `groups` (
    `id`, `displayName`
)VALUES(
    'e9e30dba-f08f-4109-8486-d5c6a331660a', 'Developer'
);

INSERT IGNORE INTO `groups_users` (
    `userId`, `groupId`
)VALUES(
    '2819c223-7f76-453a-919d-413861904646', 'e9e30dba-f08f-4109-8486-d5c6a331660a'
);