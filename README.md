
## app
#### 壳工程，不处理任何业务，将所需要的各个组件模块组合起来，构成一个完整的应用

## ModuleMain
#### 主模块工程，处理项目相关业务。可修改config.gradle中的isModule值，进行Application及Module两种模式切换

## BaseModule
#### 主要存放一些基础类和工具类，只做为Module为上层业务模块提供服务

## ModuleA
#### 业务A

## ModuleB
#### 业务B