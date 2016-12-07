#ifndef MAD_ASYNC_SEND
#define MAD_ASYNC_SEND

#include "mad_msg.h"

#ifdef __cplusplus
extern "C" {
#endif

void madAsyncInit(void);
void madAsyncSend(MadMsg *msg);

#ifdef __cplusplus
}
#endif
        

#endif /* MAD_ASYNC_SEND */
