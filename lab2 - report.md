# Lab2实验报告

2011011384 计34 郑玉昆

## 练习1

1. 请在实验报告中简要说明你的设计实现过程。请回答如下问题：你的first fit算法是否有进一步的改进空间？

- 主要设计思路

  通过一个双向的链表list来管理空闲的内存块。对给定的init,memmap,alloc_pages和free_pages进行重写。

  ``default_init`` : 对操作系统要用的数据结构进行初始化，包括对free_list链表和nr_free空闲页个数置0。

  ``default_init_memmap`` : 对内存中空闲页的初始化，即将它们加入到空闲页的列表中。对于需要初始化的每一个页，设置标志位表明该页有效，property的值表示以该页开头有多少个连续的页，除了base外都为0，先统一设置成0，将页按顺序加到链表的末尾，再按需修改base的property和nr_free的值。

  ``default_alloc_pages`` : 对从base开始的n个连续的页进行分配。从头开始遍历页表，一旦找到一个大于n个连续页的块，就开始分配。对于分配的项，设置标志表明它们已经被使用，在空闲页列表里删除当前页。如果当前空闲块的页数大于n，那么分配n个页后剩下的第一个页为新的块的形状，它的property比原来的小n。

  ``default_free_pages`` : 这个函数的作用是释放从base开始的n个连续页，并对空闲块做合并工作。按顺序寻找，找到第一个地址大于base的页的前面作为释放的块应插入的位置。对于要释放的每一个页，置引用个数为0，清除使用标志，并将它们重新加入的空闲链表当中。当前块是否能和后面的块进行合并，如果能，修改它们的property值，完成合并。

- 算法可能改进空间

  空闲页列表中存储的项是单页，而遍历的时候则是一页页遍历，通过property的值来判断块的大小，如果有一个块很大，那么需要遍历许多无用的页。
  可以考虑改进存储方式，将空闲页按连续块进行存储，而每一块中则只需要记录第一页的起始地址和块中页的个数，这样在遍历的时候可以节约空间。

## 练习二

---
1. 寻找虚拟地址对应的页表项。
```
	实现过程：实现函数get_pte(pde_t *pgdir, uintptr_t la, bool create);
	首先要查询一级页表，根据线性地址la在一级页表pgdir中寻找二级页表的起始地址。如果二级页表不存在，那么要根据参数create来分配二级页表的内存空间。
```
1. 问题：
	（1）请描述页目录项（Pag Director Entry）和页表（Page Table Entry）中每个组成部分的含义和以及对ucore而言的潜在用处。
	（2）如果ucore执行过程中访问内存，出现了页访问异常，请问硬件要做哪些事情？
```
（1）pde 由四个部分组成：页目录项内容 = 页表起始物理地址 | PTE_U \ PTE_W | PTE_P
	pte由三个部分组成：页表项内容 = (pa & 0x0FFF) | PTE_P | PTE_W
	PTE_U：位3，表示用户态的软件可以读取对应地址的物理内存页内容;PTE_W：位2，表示物理内存页内容可写;PTE_P：位1，表示物理内存页存在;潜在用处：实现地址映射。实现内核态和用户态的地址空间的分离，实现保护。
（2）硬件：保存寄存器的值，设置异常号码（error code），
```
---

## 练习三

---
1. 释放某虚地址所在的页并取消对应二级页表项的映射（需要编程）
```
	实现过程：首先检测物理内存页是否存在。存在的话，就找到对应的物理内存页，然后如果它的ref值减小至0，则将page删除；
	然后删除对应的二级页表项，刷新tlb。
```
1. 问题：
    （1）数据结构Page的全局变量（其实是一个数组）的每一项与页表中的页目录项和页表项有无对应关系？如果有，其对应关系是啥？
    （2）如果希望虚拟地址与物理地址相等，则需要如何修改lab2，完成此事？ 鼓励通过编程来具体完成这个问题
```
	（1）page的结构由flags，property，ref以及page_link.而页目录项和页表项的结果如练习一所示，他们没有对应关系。
	（2）虚拟地址和物理地址的非对等映射是因为引入了页机制，所以，如果不利用页机制的话，线性逻辑地址就等同于物理地址。
```
---


```
Kernel executable memory footprint: 99KB
ebp:0xc0116f38 eip:0xc01009d0 args:0x00010094 0x00000000 0xc0116f68 0xc01000bc 
    kern/debug/kdebug.c:310: print_stackframe+21
ebp:0xc0116f48 eip:0xc0100cbf args:0x00000000 0x00000000 0x00000000 0xc0116fb8 
    kern/debug/kmonitor.c:129: mon_backtrace+10
ebp:0xc0116f68 eip:0xc01000bc args:0x00000000 0xc0116f90 0xffff0000 0xc0116f94 
    kern/init/init.c:49: grade_backtrace2+33
ebp:0xc0116f88 eip:0xc01000e5 args:0x00000000 0xffff0000 0xc0116fb4 0x00000029 
    kern/init/init.c:54: grade_backtrace1+38
ebp:0xc0116fa8 eip:0xc0100103 args:0x00000000 0xc010002a 0xffff0000 0x0000001d 
    kern/init/init.c:59: grade_backtrace0+23
ebp:0xc0116fc8 eip:0xc0100128 args:0xc0105f5c 0xc0105f40 0x00000f32 0x00000000 
    kern/init/init.c:64: grade_backtrace+34
ebp:0xc0116ff8 eip:0xc010007f args:0x00000000 0x00000000 0x0000ffff 0x40cf9a00 
    kern/init/init.c:29: kern_init+84
memory management: default_pmm_manager
e820map:
  memory: 0009fc00, [00000000, 0009fbff], type = 1.
  memory: 00000400, [0009fc00, 0009ffff], type = 2.
  memory: 00010000, [000f0000, 000fffff], type = 2.
  memory: 07efe000, [00100000, 07ffdfff], type = 1.
  memory: 00002000, [07ffe000, 07ffffff], type = 2.
  memory: 00040000, [fffc0000, ffffffff], type = 2.
check_alloc_page() succeeded!
check_pgdir() succeeded!
check_boot_pgdir() succeeded!
-------------------- BEGIN --------------------
PDE(0e0) c0000000-f8000000 38000000 urw
  |-- PTE(38000) c0000000-f8000000 38000000 -rw
PDE(001) fac00000-fb000000 00400000 -rw
  |-- PTE(000e0) faf00000-fafe0000 000e0000 urw
  |-- PTE(00001) fafeb000-fafec000 00001000 -rw
--------------------- END ---------------------
++ setup timer interrupts
```


