USE [82]
GO

/****** Object:  Table [dbo].[task_definition]    Script Date: 12/8/2016 4:28:18 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[task_definition](
	[task_id] [nvarchar](256) NOT NULL,
	[role_id] [nvarchar](256) NULL,
	[name] [nvarchar](2000) NOT NULL,
	[task_typ] [nvarchar](1) NOT NULL,
	[cmd_line] [nvarchar](max) NOT NULL,
	[run_dir] [nvarchar](2000) NULL,
	[log_file] [nvarchar](2000) NULL,
	[trace_level] [nvarchar](60) NULL,
	[restart] [int] NOT NULL,
	[auto_start] [int] NOT NULL,
	[start_delay] [int] NULL,
	[grp_nam] [nvarchar](40) NULL,
 CONSTRAINT [task_definition_pk] PRIMARY KEY CLUSTERED 
(
	[task_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

CREATE TABLE [dbo].[task_env_definition](
	[task_id] [nvarchar](256) NOT NULL,
	[name] [nvarchar](256) NOT NULL,
	[value] [nvarchar](2000) NULL,
 CONSTRAINT [task_env_definition_pk] PRIMARY KEY CLUSTERED 
(
	[task_id] ASC,
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

create view [dbo].[role_definition] as 
select distinct coalesce(role_id, task_id) as role_id 
   from task_definition 
union
select distinct role_id as role_id 
   from job_definition 
     where role_id is not null
GO
