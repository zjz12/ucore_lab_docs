##练习2：使用qemu执行并调试lab1中的软件

为了熟悉使用qemu和gdb进行的调试工作，我们进行如下的小练习：  

1. 从CPU加电后执行的第一条指令开始，单步跟踪BIOS的执行。  
	a. 为了跟踪BIOS，需要修改lab1/tools/gdbinit, 内容为:  
	
	```
	file bin/kernel
	set architecture i8086
	target remote :1234
	break kern_init
	```
	
	b. 随后执行debug，在gdb调试界面下执行``si``命令  
	c. 在gdb界面下，可通过如下命令来看BIOS的代码  
	
	```
 	x /2i $pc  //显示当前eip处的2条汇编指令
 	```
 	
2. 在初始化位置0x7c00设置实地址断点,测试断点正常。  
	a. 修改lab1/tools/gdbinit，内容为  
	```
	file bin/kernel
	target remote :1234
	break kern_init
	continue
	set architecture i8086
	b *0x7c00
	c
	x /2i $pc
	set architecture i386
	```
	
	b. ``make debug``后得到如下结果：  
	```
	Breakpoint 2 at 0x7c00
	Breakpoint 2, 0x00007c00 in ?? ()
	=> 0x7c00:      cli    
   	0x7c01:      cld    
	The target architecture is assumed to be i386
	```
	
3. 从0x7c00开始跟踪代码运行,将单步跟踪反汇编得到的代码与bootasm.S和 bootblock.asm进行比较。
4. 自己找一个bootloader或内核中的代码位置，设置断点并进行测试。
