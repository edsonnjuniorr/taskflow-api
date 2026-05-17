CREATE TABLE app_users (
                           id UUID PRIMARY KEY,
                           name VARCHAR(120) NOT NULL,
                           email VARCHAR(254) NOT NULL,
                           CONSTRAINT uk_app_users_email UNIQUE (email)
);

CREATE TABLE tasks (
                       id UUID PRIMARY KEY,
                       title VARCHAR(160) NOT NULL,
                       description VARCHAR(1000),
                       status VARCHAR(20) NOT NULL,
                       created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                       completed_at TIMESTAMP WITH TIME ZONE,
                       user_id UUID NOT NULL,
                       CONSTRAINT fk_tasks_user_id FOREIGN KEY (user_id) REFERENCES app_users (id),
                       CONSTRAINT ck_tasks_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE TABLE subtasks (
                          id UUID PRIMARY KEY,
                          title VARCHAR(160) NOT NULL,
                          description VARCHAR(1000),
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                          completed_at TIMESTAMP WITH TIME ZONE,
                          task_id UUID NOT NULL,
                          CONSTRAINT fk_subtasks_task_id FOREIGN KEY (task_id) REFERENCES tasks (id),
                          CONSTRAINT ck_subtasks_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED'))
);