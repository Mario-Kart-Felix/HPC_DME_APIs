--
-- hpc_review_view.sql
--
-- Copyright SVG, Inc.
-- Copyright Leidos Biomedical Research, Inc
-- 
-- Distributed under the OSI-approved BSD 3-Clause License.
-- See http://ncip.github.com/HPC/LICENSE.txt for details.
--
--
-- @author <a href="mailto:yuri.dinh@nih.gov">Yuri Dinh</a>
-- @version $Id$
--

                 
DROP VIEW R_REVIEW_META_MAIN;
create view R_REVIEW_META_MAIN as
SELECT coll.coll_id                                     as id,
       coll.coll_name                                   as path,
       project_title.meta_attr_value                    as project_title,
       project_description.meta_attr_value              as project_description,
       start_date.meta_attr_value                       as project_start_date,
       data_owner.meta_attr_value                       as data_owner,
       hpc_user.FIRST_NAME || ' ' || hpc_user.LAST_NAME as data_curator_name,
       data_curator.meta_attr_value                     as data_curator,
       project_status.meta_attr_value                   as project_status,
       publications.meta_attr_value                     as publications,
       sunset_date.meta_attr_value                      as sunset_date,
       last_reviewed.meta_attr_value                    as last_reviewed,
       review_sent                                      as review_sent,
       reminder_sent                                    as reminder_sent
FROM r_coll_main coll
         join r_coll_main parent_coll on coll.PARENT_COLL_NAME = parent_coll.COLL_NAME
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main project_title ON project_title.meta_id = map.meta_id AND
                                      (project_title.meta_attr_name = 'project_title' or
                                       project_title.meta_attr_name = 'project_name'))
                   ON map.object_id = coll.coll_id
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main project_description ON project_description.meta_id = map.meta_id AND
                                            (project_description.meta_attr_name = 'project_description' or
                                             project_description.meta_attr_name = 'description'))
                   ON map.object_id = coll.coll_id
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main start_date ON start_date.meta_id = map.meta_id AND
                                   (start_date.meta_attr_name = 'start_date' or
                                    start_date.meta_attr_name = 'project_start_date'))
                   ON map.object_id = coll.coll_id
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main data_owner ON data_owner.meta_id = map.meta_id AND
                                   data_owner.meta_attr_name = 'data_owner')
                   ON map.object_id = parent_coll.coll_id
         JOIN (r_objt_metamap map
    JOIN r_meta_main data_curator ON data_curator.meta_id = map.meta_id AND
                                     data_curator.meta_attr_name = 'data_curator')
              ON map.object_id = parent_coll.coll_id
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main project_status ON project_status.meta_id = map.meta_id AND
                                       project_status.meta_attr_name = 'project_status')
                   ON map.object_id = coll.coll_id
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main publications ON publications.meta_id = map.meta_id AND
                                     (publications.meta_attr_name = 'publications' or
                                      publications.meta_attr_name = 'publication'))
                   ON map.object_id = coll.coll_id
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main sunset_date ON sunset_date.meta_id = map.meta_id AND
                                    sunset_date.meta_attr_name = 'sunset_date')
                   ON map.object_id = coll.coll_id
         LEFT JOIN (r_objt_metamap map
    JOIN r_meta_main last_reviewed ON last_reviewed.meta_id = map.meta_id AND
                                      last_reviewed.meta_attr_name = 'last_reviewed')
                   ON map.object_id = coll.coll_id
         JOIN (r_objt_metamap map
    JOIN r_meta_main collection_type ON collection_type.meta_id = map.meta_id AND
                                        collection_type.meta_attr_name = 'collection_type' AND
                                        collection_type.meta_attr_value = 'Project')
              ON map.object_id = coll.coll_id
         LEFT JOIN HPC_USER hpc_user ON data_curator.meta_attr_value = hpc_user.USER_ID
         LEFT JOIN (select USER_ID, LISTAGG(to_char(delivered, 'MM/DD/YY'), ', ') as review_sent
                    from HPC_NOTIFICATION_REVIEW
                    where event_type = 'REVIEW_SENT'
                    group by USER_ID) review_event on review_event.USER_ID = data_curator.meta_attr_value
         LEFT JOIN (select USER_ID, LISTAGG(to_char(delivered, 'MM/DD/YY'), ', ') as reminder_sent
                    from HPC_NOTIFICATION_REVIEW
                    where event_type = 'REVIEW_REMINDER_SENT'
                    group by USER_ID) reminder_event on reminder_event.USER_ID = data_curator.meta_attr_value;
                    
                    
CREATE TABLE "IRODS"."HPC_NOTIFICATION_REVIEW"
(
   USER_ID VARCHAR2(50) NOT NULL,
   EVENT_TYPE VARCHAR2(50) NOT NULL,
   DELIVERED timestamp NOT NULL;
)
;
comment on table IRODS."HPC_NOTIFICATION_REVIEW" is 'Review Notification sent - i.e. a list of all review notifications the system sent out';

comment on column IRODS."HPC_NOTIFICATION_REVIEW"."USER_ID" is 'The user ID that was notified';

comment on column IRODS."HPC_NOTIFICATION_REVIEW"."EVENT_TYPE" is 'The event type that notification was sent for';

comment on column IRODS."HPC_NOTIFICATION_DELIVERY_RECEIPT"."DELIVERED" is 'The data / time the notification delivery was performed';
