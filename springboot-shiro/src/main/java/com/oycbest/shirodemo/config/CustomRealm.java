package com.oycbest.shirodemo.config;

import com.oycbest.shirodemo.domain.Permission;
import com.oycbest.shirodemo.service.UserRoleVoService;
import com.oycbest.shirodemo.vo.RolePermissinVo;
import com.oycbest.shirodemo.vo.UserRolerVo;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: oyc
 * @Date: 2020-06-02 16:12
 * @Description:
 */
public class CustomRealm extends AuthorizingRealm {

	@Autowired
	private UserRoleVoService userRoleVoService;

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
		//获取登录用户名
		String name = (String) principalCollection.getPrimaryPrincipal();
		//根据用户名去数据库查询用户信息
		UserRolerVo user = userRoleVoService.queryByAccount(name);
		// 添加角色和权限
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
		for (RolePermissinVo role: user.getRoles()) {
			//添加角色
			simpleAuthorizationInfo.addRole(role.getRoleKey());
			//添加权限
			for (Permission permission : role.getPerms()) {
				if (permission != null && permission.getPerms() !=null && permission.getPerms()!=""){
					simpleAuthorizationInfo.addStringPermission(permission.getPerms());
				}
			}
		}
		return simpleAuthorizationInfo;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
		//加这一步的目的是在Post请求的时候会先进认证，然后在到请求
		if (authenticationToken.getPrincipal() == null) {
			return null;
		}
		//获取用户信息
		String name = authenticationToken.getPrincipal().toString();
		UserRolerVo user = userRoleVoService.queryByAccount(name);
		if (user == null) {
			//这里返回后会报出对应异常
			return null;
		} else {
			//这里验证authenticationToken和simpleAuthenticationInfo的信息
			return new SimpleAuthenticationInfo(name, user.getPassword(), getName());
		}
	}
}
