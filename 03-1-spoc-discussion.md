```C
#include <sys/types.h>
#include <unistd.h>

#define BLOCK_SIZE 32//24 /* 由于存在虚拟的data字段，sizeof不能正确计算meta长度，这里手工设置 */

typedef struct s_block *t_block;
struct s_block {
    size_t size;  /* 数据区大小 */
    t_block prev; /* 指向上个块的指针 */
    t_block next; /* 指向下个块的指针 */
    int free;     /* 是否是空闲块,1表示空闲 */
    int padding;  /* 填充4字节，保证meta块长度为8的倍数 */
    //void *ptr;    /* Magic pointer，指向data */
    char data[1];  /* 这是一个虚拟字段，表示数据块的第一个字节，长度不应计入meta */
};

void *first_block = NULL;

/* First fit */
t_block zfind_block(t_block *last, size_t size) {
    t_block b = (t_block) first_block;
    while(b && !(b->free && b->size >= size)) {
        *last = b;
        b = b->next;
    }
    return b;
}

/*开辟新的block*/
t_block zextend_heap(t_block last, size_t s) {
    t_block b;
    (void *)b = sbrk(0);
    if(sbrk(BLOCK_SIZE + s) == (void *)-1)
        return NULL;
    b->size = s;
    b->next = NULL;
    b->prev = NULL;
    if(last) {
        last->next = b;
        b->prev = last;
    }
    b->free = 0;
    return b;
}
/*分裂block*/
void zsplit_block(t_block b, size_t s) {
    t_block new_block;
    new_block = (t_block) (b->data + s);
    new_block->size = b->size - s - BLOCK_SIZE ;
    new_block->next = b->next;
    new_block->prev = b;
    new_block->free = 1;
    b->size = s;
    b->next = new_block;
}

size_t align8(size_t s) {
    if(s & 0x7 == 0)        //若已经8bit对齐
        return s;
    return ((s >> 3) + 1) << 3;
}

void * zmalloc(size_t size) {
    t_block b, last;
    size_t s;
    /* 对齐地址 */
    s = align8(size);
    if(first_block) {
        /* 查找合适的block */
        last = (t_block) first_block;
        b = zfind_block(&last, s);
        if(b) {
            /* 如果可以，则分裂 */
            if ((b->size - s) >= ( BLOCK_SIZE + 8))
                zsplit_block(b, s);
            b->free = 0;
            b->prev = last;
        } else {
            /* 没有合适的block，开辟一个新的 */
            b = zextend_heap(last, s);
            if(!b)
                return NULL;
        }
    } else {
        b = zextend_heap(NULL, s);
        if(!b)
            return NULL;
        first_block = b;
    }
    return b->data;
}

t_block zget_block(void *p) {
    char *tmp;
    (void *)tmp = p;
    return (t_block) (p = tmp -= BLOCK_SIZE);
}
 /*检查地址合法性*/
int zvalid_addr(void *p) {
    if(first_block) {
        if(p > first_block && p < sbrk(0)) {
            return p == (void *)(zget_block(p))->data;
        }
    }
    return 0;
}
/*合并相邻空闲块*/
t_block zfusion(t_block b) {
    if (b->next && b->next->free) {
        b->size += BLOCK_SIZE + b->next->size;
        b->next = b->next->next;
        if(b->next)
            b->next->prev = b;
    }
    return b;
}
void zfree(void *p) {
    t_block b;
    if(zvalid_addr(p)) {
        b = zget_block(p);
        b->free = 1;
        if(b->prev && b->prev->free)
            b = zfusion(b->prev);
        if(b->next)
            zfusion(b);
        else {
            if(b->prev)
                b->prev->prev = NULL;
            else
                first_block = NULL;
            brk(b);
        }
    }
}
```
