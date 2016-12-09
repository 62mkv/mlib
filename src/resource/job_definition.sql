USE [82]
GO

/****** Object:  Table [dbo].[job_definition]    Script Date: 12/8/2016 4:29:44 PM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[job_definition](
	[job_id] [nvarchar](256) NOT NULL,
	[role_id] [nvarchar](256) NULL,
	[name] [nvarchar](2000) NOT NULL,
	[enabled] [int] NOT NULL,
	[type] [nvarchar](60) NOT NULL,
	[command] [nvarchar](max) NOT NULL,
	[log_file] [nvarchar](2000) NULL,
	[trace_level] [nvarchar](60) NULL,
	[overlap] [int] NOT NULL,
	[schedule] [nvarchar](2000) NULL,
	[start_delay] [int] NULL,
	[timer] [int] NULL,
	[grp_nam] [nvarchar](40) NULL,
 CONSTRAINT [job_definition_pk] PRIMARY KEY CLUSTERED 
(
	[job_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

CREATE TABLE [dbo].[job_env_definition](
	[job_id] [nvarchar](256) NOT NULL,
	[name] [nvarchar](256) NOT NULL,
	[value] [nvarchar](2000) NULL,
 CONSTRAINT [job_env_definition_pk] PRIMARY KEY CLUSTERED 
(
	[job_id] ASC,
	[name] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO

CREATE TABLE [dbo].[job_definition_exec](
        [job_id] [nvarchar](256) NOT NULL,
        [node_url] [nvarchar](256) NOT NULL,
        [start_dte] [datetime] NOT NULL,
        [status] [int] NULL,
        [message] [nvarchar](2000) NULL,
        [end_dte] [datetime] NULL,
     CONSTRAINT [job_definition_exec_pk] PRIMARY KEY CLUSTERED 
    (
        [job_id] ASC,
        [node_url] ASC,
        [start_dte] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
    ) ON [PRIMARY]

GO