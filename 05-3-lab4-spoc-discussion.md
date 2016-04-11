# lab4 spoc 思考题

2011011384 郑玉昆
2013011375 茹逸中

## 个人思考题

### 总体介绍

(1) ucore的线程控制块数据结构是什么？

```C
struct proc_struct {
    enum proc_state state;                      // Process state
    int pid;                                    // Process ID
    int runs;                                   // the running times of Proces
    uintptr_t kstack;                           // Process kernel stack
    volatile bool need_resched;                 // bool value: need to be rescheduled to release CPU?
    struct proc_struct *parent;                 // the parent process
    struct mm_struct *mm;                       // Process's memory management field
    struct context context;                     // Switch here to run process
    struct trapframe *tf;                       // Trap frame for current interrupt
    uintptr_t cr3;                              // CR3 register: the base addr of Page Directroy Table(PDT)
    uint32_t flags;                             // Process flag
    char name[PROC_NAME_LEN + 1];               // Process name
    list_entry_t list_link;                     // Process link list 
    list_entry_t hash_link;                     // Process hash list
};
```

### 关键数据结构

(2) 如何知道ucore的两个线程同在一个进程？

不同进程的``mm``和``cr3``分别指向不同的内存空间和页目录基地址，所以只需要看这两个变量是否相同即可。

(3) context和trapframe分别在什么时候用到？

context在进程切换时用到，如``switch.S``

trapframe在产生中断（异常、系统调用）时保存当前进程信息，用于进程切换

(4) 用户态或内核态下的中断处理有什么区别？在trapframe中有什么体现？

内核态中断：特权级不变，trapframe少存SS和ESP；

### 执行流程

(5) do_fork中的内核线程执行的第一条指令是什么？它是如何过渡到内核线程对应的函数的？

```
tf.tf_eip = (uint32_t) kernel_thread_entry;
/kern-ucore/arch/i386/init/entry.S
/kern/process/entry.S
```

(6)内核线程的堆栈初始化在哪？

```
tf和context中的esp
```

(7)fork()父子进程的返回值是不同的。这在源代码中的体现中哪？

父进程调用``fork()``的时候，``ret = proc->pid;``， 可知父进程能得到子进程的pid。

子进程在``do_fork()``中调用``copy_thread()``时，``proc->tf->tf_regs.reg_eax = 0``，说明子进程返回值为0。

(8)内核线程initproc的第一次执行流程是什么样的？能跟踪出来吗？

