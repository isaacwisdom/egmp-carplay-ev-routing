/* SPDX-License-Identifier: GPL-3.0-or-later
 * Extend the stock identification parameter tables at serialization time.
 * Unrelated root parameters, callbacks and native framing stay with the OEM.
 */
typedef const Param *(*Children)(u16,u16,u16 *,void *,int *);
static const Param *root_table,*info_table,*status_table;
static Lookup root_lookup,info_lookup,status_lookup;
static Builder raw_builder;
static u32 *hook_mailbox;
static int (*protect_page)(void *,u32,int);
/* HMG's pokeNative restores code pages to RX. Only the data word is changed;
 * retain execute permission during the write, then restore RX before returning. */
static int set_handler(u32 value){
    if(!hook_mailbox || !protect_page)return -18;
    void *page=(void*)((u32)hook_mailbox&~4095U);
    if(protect_page(page,4096,7))return -19;
    __atomic_store_n(hook_mailbox,value,__ATOMIC_RELEASE);
    if(protect_page(page,4096,5)){
        __atomic_store_n(hook_mailbox,0,__ATOMIC_RELEASE);
        protect_page(page,4096,5);
        return -19;
    }
    return 0;
}
static u32 identity_mutex;
static int configured;
static char display_name[321];
static u32 dc_w,ac_w,engine=2,connector;
static Param info_copy[9],status_copy[15];
static void connector_ccs(u16 a,u16 b,u32 c,void *d){(void)a;(void)b;(void)c;(void)d;connector=0;}
static void connector_ac(u16 a,u16 b,u32 c,void *d){(void)a;(void)b;(void)c;(void)d;connector=2;}
static void *info_value(u16 id,u16 type,u16 *n,void *key,int *more){
    if(more)*more=0;
    if(id==2)return &engine;
    if(id==6 || id==8)return display_name;
    if(id==11)return &connector;
    if(id==12)return &dc_w;
    if(id==14)return &ac_w;
    return info_lookup(id,type,n,key,more);
}
static void *status_value(u16 id,u16 type,u16 *n,void *key,int *more){return status_lookup(id,type,n,key,more);}
static const Param *identity_children(u16 id,u16 type,u16 *count,void *key,int *more){
    (void)type;(void)key;if(more)*more=0;
    if(id==20){*count=9;return info_copy;}
    if(id==21){*count=15;return status_copy;}
    return 0;
}
static void *identity_value(u16 id,u16 type,u16 *n,void *key,int *more){
    if(id==20){if(more)*more=0;return (void*)info_value;}
    if(id==21){if(more)*more=0;return (void*)status_value;}
    return root_lookup(id,type,n,key,more);
}
static Param parameter(u16 id,u16 type){Param p={1,id,type,0,0,0,0,0};return p;}
static u32 identity_builder(u32 handle,u32 session,u32 a,u32 b,u32 command,void *table,u32 count,const char *name,Lookup lookup){
    if(!__atomic_load_n(&configured,__ATOMIC_ACQUIRE) || command!=0x1d01 || table!=root_table || count!=24)
        return raw_builder(handle,session,a,b,command,table,count,name,lookup);
    if(api.lock(&identity_mutex))return 0;
    Param root[24];for(u32 i=0;i<24;i++){
        root[i]=root_table[i];
        if(root[i].id==20 || root[i].id==21){root[i].used=1;root[i].children=(u32)identity_children;}
    }
    u32 result=raw_builder(handle,session,a,b,command,root,24,name,identity_value);
    api.unlock(&identity_mutex);return result;
}
/* The recipe's gate first falls through the original entry instruction. Arming
 * this aligned data word redirects it only after all bindings are ready. */
static Builder recipe_original(Builder entry){
    if(!entry)return 0;
    u16 *p=(u16*)((u32)entry&~1U);
    if((p[0]&0xf800)!=0xf000 || (p[1]&0xd000)!=0x9000)return 0;
    u32 s=(p[0]>>10)&1,j1=(p[1]>>13)&1,j2=(p[1]>>11)&1;
    u32 bits=(s<<24)|((!(j1^s))<<23)|((!(j2^s))<<22)|((p[0]&1023)<<12)|((p[1]&2047)<<1);
    int delta=(int)(bits<<7)>>7;
    char *gate=(char*)p+4+delta;u32 *header=(u32*)(gate+16);
    if(header[0]!=0x45564731 || header[1]!=1 || header[2]<32 || header[2]>128 || header[3]!=0)return 0;
    hook_mailbox=&header[3];return (Builder)((u32)(gate+header[2])|1);
}
static int identity_bind(void *lib,void *libc,Builder entry){
    protect_page=dlsym(libc,"mprotect");if(!protect_page)return -18;
    raw_builder=recipe_original(entry);if(!raw_builder)return -18;
    root_table=dlsym(lib,"iAP2IdenInfoMsgList");
    root_lookup=dlsym(lib,"_Z26iAP2ControlIden_GetPointerttPtPvPi");
    info_lookup=dlsym(lib,"_Z41iAP2ControlIden_VehicleInfoIdenGetPointerttPtPvPi");
    status_lookup=dlsym(lib,"iAP2ControlIden_VehicleStatusGetPointer");
    const Param *(*get_info)(u32*)=dlsym(lib,"iAP2ControlIden_GetVehicleInfoParamList");
    const Param *(*get_status)(u32*)=dlsym(lib,"iAP2ControlIden_GetVehicleStatusParamList");
    if(!root_table || !root_lookup || !info_lookup || !status_lookup || !get_info || !get_status)return -18;
    u32 n=0;info_table=get_info(&n);if(n!=5 || !info_table)return -18;
    status_table=get_status(&n);if(n!=13 || !status_table)return -18;
    return 0;
}
static int identity_configure(const char *name,u32 len,u32 dc,u32 ac){
    if(!hook_mailbox || !string_ok(name,len) || len>320 || !dc || dc>1000000 || !ac || ac>1000000)return -12;
    if(api.lock(&identity_mutex))return -15;
    copy(display_name,name,len);display_name[len]=0;dc_w=dc;ac_w=ac;
    for(u32 i=0;i<5;i++){
        info_copy[i]=info_table[i];
        if(info_copy[i].id==2 || info_copy[i].id==6 || info_copy[i].id==8){info_copy[i].used=1;info_copy[i].init=info_copy[i].length=info_copy[i].done=0;}
    }
    info_copy[5]=parameter(11,8);info_copy[5].init=(u32)connector_ccs;
    info_copy[6]=parameter(11,8);info_copy[6].init=(u32)connector_ac;
    info_copy[7]=parameter(12,6);info_copy[8]=parameter(14,6);
    for(u32 i=0;i<13;i++){status_copy[i]=status_table[i];u16 id=status_copy[i].id;if(id==3 || id==6 || id==11 || id==15)status_copy[i].used=1;}
    status_copy[13]=parameter(21,12);status_copy[14]=parameter(30,12);
    __atomic_store_n(&configured,1,__ATOMIC_RELEASE);
    int result=set_handler((u32)identity_builder);
    if(result)__atomic_store_n(&configured,0,__ATOMIC_RELEASE);
    api.unlock(&identity_mutex);return result;
}
