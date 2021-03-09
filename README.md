# PurePullRefreshView
# PureImageView
### 功能介绍
### 效果图
### 如何使用
  #### Step 1.工程目录中build.gradle在repositories字段中末端添加:
```java
allprojects {
    repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
  #### Step 2.app目录中build.gradle在添加依赖:
```java
dependencies {
    implementation 'com.github.Processama:PurePullRefreshView:v1.1'
}
```
  #### Step 3.使用该控件指定img类型等属性
### 总结
    关于这个PullRefreshView的介绍就到此为止了哈，使用起来比较简单，后续有bug或新功能再更新哦！！！
