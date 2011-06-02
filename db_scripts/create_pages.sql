CREATE  TABLE `page_link_revisions`.`pages` (
  `page_id` INT NOT NULL ,
  `page_title` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`page_id`) ,
  UNIQUE INDEX `page_id_UNIQUE` (`page_id` ASC) )
ENGINE = InnoDB;
