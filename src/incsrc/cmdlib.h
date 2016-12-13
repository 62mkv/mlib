/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for cmdlib.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 20163
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#ifndef CMDLIB_H
#define CMDLIB_H

#include <mislib.h>
#include <srvlib.h>

/*
 *  Load Definitions
 */

#define CMD_LOAD_ALL       0
#define CMD_LOAD_LEVELS    1
#define CMD_LOAD_COMMANDS  2
#define CMD_LOAD_TRIGGERS  3

/*
 *  Analyze Level Definitions
 */

#define CMD_CHECK_QUIET           0L
#define CMD_CHECK_SHOW_PROTOTYPE  1L
#define CMD_CHECK_SHOW_SYNTAX     2L
#define CMD_CHECK_PROTOTYPE       4L
#define CMD_CHECK_SYNTAX          8L

#define CMD_CHECK_DEFAULT         (CMD_CHECK_PROTOTYPE | CMD_CHECK_SYNTAX)

/*
 * Keyword Definitions
 */

#define KW_ALIAS           "alias"
#define KW_ARGUMENT        "argument"
#define KW_CLASS           "class"
#define KW_COMMAND         "command"
#define KW_COMPONENT_LEVEL "component-level"
#define KW_DATATYPE        "datatype"
#define KW_DEFAULT_VALUE   "default-value"
#define KW_DESCRIPTION     "description"
#define KW_DISABLE         "disable"
#define KW_DOCUMENTATION   "documentation"
#define KW_ENABLE          "enable"
#define KW_FIRE_SEQUENCE   "fire-sequence"
#define KW_FUNCTION        "function"
#define KW_INSECURE        "insecure"
#define KW_LIBRARY         "library"
#define KW_LOCAL_SYNTAX    "local-syntax"
#define KW_METHOD          "method"
#define KW_NAME            "name"
#define KW_NAMESPACE       "namespace"
#define KW_PACKAGE         "package"
#define KW_ON_COMMAND      "on-command"
#define KW_PROGRAM_ID      "program-id"
#define KW_READ_ONLY       "read-only"
#define KW_REQUIRED        "required"
#define KW_SORT_SEQUENCE   "sort-sequence"
#define KW_TRIGGER         "trigger"
#define KW_TYPE            "type"
#define KW_VERSION         "version"

/*
 * Component Type Definitions
 */

#define CT_LOCAL_SYNTAX          "Local Syntax"
#define CT_C_FUNCTION            "C Function"
#define CT_SIMPLE_C_FUNCTION     "Simple C Function"
#define CT_MANAGED_METHOD        "Managed Method"
#define CT_SIMPLE_MANAGED_METHOD "Simple Managed Method"
#define CT_COM_METHOD            "COM Method"
#define CT_JAVA_METHOD           "Java Method"
#define CT_UNKNOWN               "Unknown"

/*
 * Data Type Definitions
 */

#define DT_FLAG    "flag"
#define DT_INTEGER "integer"
#define DT_FLOAT   "float"
#define DT_STRING  "string"
#define DT_POINTER "pointer"
#define DT_RESULTS "results"
#define DT_OBJECT  "object"
#define DT_UNKNOWN "unknown"
#define DT_BINARY  "binary"

/*
 *  Command Repository Type Definition
 */

typedef struct
{
    long levelsCount,
         commandsCount,
         triggersCount;
    void *levels,
         *commands,
         *triggers;
} CommandRepository;

/*
 * Component Level Information Type Definition
 */

typedef struct
{
    char *name;
    char *description;
    char *pathname;
    char *nameSpace;
    char *package;
    char *progid;
    char *directory;
    char *library;
    char *version;
    long sortseq;
    long readonly;
} ComponentLevelInfo;

/*
 * Argument Information Type Definition
 */

typedef struct
{
    char *name;
    char *alias;
    char *description;
    char *value;
    char *datatype;
    long index;
    long required;
} ArgumentInfo;

/*
 * Command Information Type Definition
 */

typedef struct
{
    char *name;
    char *description;
    char *pathname;
    char *level;
    char *type;
    char *syntax;
    char *classname;
    char *function;
    char *documentation;
    char *version;
    long disable;
    long insecure;
    long readonly;
    long argCount;
    ArgumentInfo **args;
} CommandInfo;

/*
 * Trigger Information Type Definition
 */

typedef struct
{
    char *name;
    char *command;
    char *description;
    char *pathname;
    char *level;
    char *syntax;
    char *documentation;
    char *version;
    long enable;
    long disable;
    long readonly;
    long sequence;
    long argCount;
    ArgumentInfo **args;
} TriggerInfo;

/*
 *  Function Prototypes
 */

#if defined(__cplusplus)
extern "C" {
#endif

/* cmdAddCommand.c */
long cmdAddCommand(CommandRepository *repository,
                   char *level,
                   char *command,
                   long disable,
                   long insecure,
                   char *type,
                   char *syntax,
                   char *function,
                   char *method,
                   char *description,
                   char *documentation,
		   char *version);

/* cmdAddCommandArgument.c */
long cmdAddCommandArgument(CommandRepository *repository,
			   char *level,
			   char *command,
                           char *argument,
                           char *alias,
                           char *value,
                           char *datatype,
                           long index,
                           long required,
                           char *description);

/* cmdAddComponentLevel.c */
long cmdAddComponentLevel(CommandRepository *repository,
                          char *directory,
                          char *level,
                          char *nameSpace,
                          char *progid,
                          long sortseq,
                          char *description,
			  char *version);

/* cmdAddTrigger.c */
long cmdAddTrigger(CommandRepository *repository,
                   char *level,
                   char *trigger,
                   char *command,
                   long enable,
                   long disable,
                   long sequence,
                   char *syntax,
                   char *description,
                   char *documentation,
		   char *version);

/* cmdAddTriggerArgument.c */
long cmdAddTriggerArgument(CommandRepository *repository,
			   char *level,
			   char *trigger,
			   char *command,
                           char *argument,
                           char *alias,
                           char *value,
                           char *datatype,
                           long index,
                           long required,
                           char *description);

/* cmdChangeCommand.c */
long cmdChangeCommand(CommandRepository *repository,
		      char *level,
		      char *name,
                      long disable,
                      long insecure,
                      char *type,
                      char *syntax,
                      char *function,
                      char *method,
                      char *description,
                      char *documentation,
		      char *version);

/* cmdChangeCommandArgument.c */
long cmdChangeCommandArgument(CommandRepository *repository,
			      char *level,
			      char *command,
                              char *argument,
                              char *alias,
                              char *value,
                              char *datatype,
			      long index,
                              long required,
                              char *description);

/* cmdChangeComponentLevel.c */
long cmdChangeComponentLevel(CommandRepository *repository,
			     char *level,
                             char *nameSpace,
                             char *progid,
                             long sortseq,
                             char *description,
			     char *version);

/* cmdChangeTrigger.c */
long cmdChangeTrigger(CommandRepository *repository,
                      char *level,
                      char *trigger,
                      char *command,
                      long enable,
                      long disable,
                      long sequence,
                      char *syntax,
                      char *description,
                      char *documentation,
		      char *version);

/* cmdChangeTriggerArgument.c */
long cmdChangeTriggerArgument(CommandRepository *repository,
			      char *level,
			      char *trigger,
			      char *command,
                              char *argument,
                              char *alias,
                              char *value,
                              char *datatype,
			      long index,
                              long required,
                              char *description);

/* cmdCheckMemoryFile.c */
long cmdCheckMemoryFile(char *pathname, long options);

/* cmdFreeArgument.c */
void cmdFreeArgument(ArgumentInfo *arg);

/* cmdFreeCommand.c */
void cmdFreeCommand(CommandInfo *cmd);

/* cmdFreeCommandRepository.c */
void cmdFreeCommandRepository(CommandRepository *repository);

/* cmdFreeComponentLevel.c */
void cmdFreeComponentLevel(ComponentLevelInfo *lvl);

/* cmdFreeTrigger.c */
void cmdFreeTrigger(TriggerInfo *trg);

/* cmdGetArgumentList.c */
ArgumentInfo **cmdGetArgumentList(CommandInfo **cmdList,
                                  TriggerInfo **trgList,
                                  long cmdCount,
                                  long trgCount,
                                  long *argCount);

/* cmdGetCommand.c */
CommandInfo *cmdGetCommand(CommandRepository *repository, 
			   char *level,
                           char *command);

/* cmdGetCommandArgument.c */
ArgumentInfo *cmdGetCommandArgument(CommandRepository *repository,
                                    char *level,
                                    char *command,
			            char *argument);

/* cmdGetCommandList.c */
CommandInfo **cmdGetCommandList(CommandRepository *repository, long *cmdCount);

/* cmdGetCommandRepository.c */
CommandRepository *cmdGetCommandRepository(CommandRepository *repository,
					   char *start, 
					   char *level, 
					   long levelsOnly,
					   long loadDocumentation);

/* cmdGetComponentLevel.c */
ComponentLevelInfo *cmdGetComponentLevel(CommandRepository *repository,
                                         char *level);

/* cmdGetComponentLevelList.c */
ComponentLevelInfo **cmdGetComponentLevelList(CommandRepository *repository,
                                              long *lvlCount);

/* cmdGetSyntaxList.c */
char **cmdGetSyntaxList(CommandInfo **cmdList,
                        TriggerInfo **trgList,
                        long cmdCount,
                        long trgCount,
                        long *synCount);

/* cmdGetTrigger.c */
TriggerInfo *cmdGetTrigger(CommandRepository *repository,
                           char *level,
                           char *trigger,
                           char *command);

/* cmdGetTriggerArgument.c */
ArgumentInfo *cmdGetTriggerArgument(CommandRepository *repository,
                                    char *level,
                                    char *trigger,
                                    char *command,
			            char *argument);

/* cmdGetTriggerList.c */
TriggerInfo **cmdGetTriggerList(CommandRepository *repository, long *trgCount);

/* cmdRemoveCommand.c */
long cmdRemoveCommand(CommandRepository *repository, 
		      char *level,
		      char *command);

/* cmdRemoveCommandArgument.c */
long cmdRemoveCommandArgument(CommandRepository *repository, 
			      char *level,
			      char *command,
			      char *argument);

/* cmdRemoveTrigger.c */
long cmdRemoveTrigger(CommandRepository *repository, 
		      char *level,
		      char *trigger,
		      char *command);

/* cmdRemoveTriggerArgument.c */
long cmdRemoveTriggerArgument(CommandRepository *repository, 
			      char *level,
			      char *trigger,
			      char *command,
			      char *argument);

/* cmdWriteCommand.c */
long cmdWriteCommand(CommandRepository *repository, char *level, char *command);

/* cmdWriteComponentLevel.c */
long cmdWriteComponentLevel(CommandRepository *repository, char *level);

/* cmdWriteDocumentation.c */
long cmdWriteDocumentation(CommandRepository *repository, char *dir);

/* cmdWriteMemoryFile.c */
long cmdWriteMemoryFile(CommandRepository *repository, char *pathname);

/* cmdWriteTrigger.c */
long cmdWriteTrigger(CommandRepository *repository, 
		     char *level, 
		     char *trigger, 
		     char *command);

#if defined(__cplusplus)
}
#endif

#endif
