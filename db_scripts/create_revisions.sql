REATE TABLE `page_revisions` (
  `revision_id` int(11) NOT NULL,
  `revision_timestamp` datetime NOT NULL,
  `page_id` int(11) NOT NULL,
  `revision_links` mediumtext,
  PRIMARY KEY (`revision_id`),
  UNIQUE KEY `revision_id_UNIQUE` (`revision_id`),
  KEY `page_id` (`page_id`),
  CONSTRAINT `page_id` FOREIGN KEY (`page_id`) REFERENCES `pages` (`page_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$
