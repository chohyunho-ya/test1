package com.tocsg.bsd2.tool.exp.csv.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tocsg.bsd2.tool.exp.csv.dao.mapper.BsUserInfoMapper;
import com.tocsg.bsd2.tool.exp.csv.parser.BsUserInfoPo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BsUserService {

	@Value("${siteinfo.sitecode}")
	String siteCode;

	@Value("${siteinfo.domaincode}")
	String domainCode;

	@Value("${file.path}")
	String filePath;

	@Value("${file.user-filename}")
	String userFileName;
	
	@Value("${ad.ip:172.30.11.6}")
	String adIp;
	@Value("${ad.port:389}")
	String adPort;
	@Value("${ad.id:bsdadmin}")
	String adId;
	@Value("${ad.pw:qhdks1xla20241@}")
	String adPw;
	@Value("${ad.baserdn:OU=HUGEL,DC=HUGEL,DC=CO,DC=KR}")
	String adBaseRDN;
	@Value("${ad.charset:UTF-8}")
	String adCharset;
	
	
	@Autowired
	FileBackup fileBackup;

	public void userMigration() throws Exception {
		
		String userFile = filePath + userFileName;
		// 파일 생성
		File file = new File(filePath);			
		if(!file.exists()) file.mkdirs();	
		
		OutputStream os;
		
		try {
			String url = "ldap://" + adIp + ":" + adPort;
			log.info("LDAP : {}", url);
			String usrId = adId;
			String usrPw = adPw;
		 	String baseRdn = adBaseRDN;
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, url);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, usrId);
			env.put(Context.SECURITY_CREDENTIALS, usrPw);
			LdapContext ctx = new InitialLdapContext(env, null);
			 
			log.info("Active Directory Connection: CONNECTED");
			SearchControls ctls = new SearchControls();
			ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctls.setTimeLimit(300000);
			String searchFilter = String.format("(objectClass=user)");
			NamingEnumeration<?> results = ctx.search(baseRdn, searchFilter, ctls);
		
			try {
				os = new FileOutputStream(userFile);
		        PrintWriter w = new PrintWriter(new OutputStreamWriter(os, adCharset));
		      
		        int i = 0; 
		        w.print("revision_state,dept_code,position_code,user_id,user_name,email_addr,trainning_yn,dept_header_yn,dept_header_user_name,dept_header_dept_code");
		        w.print("\n");
				log.info("=====================================USER START==========================================");
				while (results.hasMore())
	            {
					SearchResult result = (SearchResult) results.next();
	                Attributes attrs = result.getAttributes();	
	                
					String userNm = "";
					if(attrs.get("name") != null)
					{
						userNm = String.valueOf(attrs.get("name").get());
					}
					
					String userId = "";
					if(attrs.get("sAMAccountName") != null)
					{
						userId = String.valueOf(attrs.get("sAMAccountName").get());
					} 		
					
					String userEmail = "";
					if(attrs.get("mail") != null)
					{
						userEmail = String.valueOf(attrs.get("mail").get());
					} 
					
					String positionNm = "";
					if(attrs.get("title") != null)
					{
						positionNm = String.valueOf(attrs.get("title").get());
					} 
					
					String positionCode = hashCode(positionNm);
					
					String deptNm = "";
					if(attrs.get("department") != null)
					{
						deptNm = String.valueOf(attrs.get("department").get());
					} else {
						deptNm = "HUGEL";
					}
					
					String deptCode = hashCode(deptNm);
					
					String deptManager = "";
					if(attrs.get("manager") != null)
					{
						deptManager = String.valueOf(attrs.get("manager").get());
					}
					String deptheaderUserNm = "";
					String deptheaderDeptCode = "";
					
					if(deptManager != "")
					{
						String[] array = deptManager.split(",");
						deptheaderUserNm = array[0].replace("CN=", "");
						deptheaderDeptCode = hashCode(array[2].replace("OU=", ""));
					}
				
					String accountExpires = "";
					if(attrs.get("accountExpires") != null) {
						accountExpires =  String.valueOf(attrs.get("accountExpires").get());
						log.info("accountExpires : {} , deptNm : {}" , accountExpires, deptNm );
					}
					
					if( positionNm == "" || userEmail == "" )
					{
						
					} 
					else if ( !"9223372036854775807".equals(accountExpires) && !"0".equals(accountExpires) )
					{
					
					}
					else {
					    log.info("ROW : " + (i + 1) + "," + "U" +  "," + deptCode + "," + positionCode + "," + userId + "," + userNm + "," + userEmail + "," + "Y" +  "," +"N" +  "," + deptheaderUserNm + "," + deptheaderDeptCode);
					    
		                w.print("I");
		        		w.print(",");
		        		w.print(deptCode);
		        		w.print(",");
		        		w.print(positionCode);
		        		w.print(",");
		        		w.print(userId);
		        		w.print(",");
		        		w.print(userNm);
		        		w.print(",");
		        		w.print(userEmail);
		        		w.print(",");
		        		w.print("Y");
		        		w.print(",");
		        		w.print("N");
		        		w.print(",");
		        		w.print(deptheaderUserNm);
		        		w.print(",");
		        		w.print(deptheaderDeptCode);
		        		w.print("\n");     		                
		                i++;
					}
	            }
				log.info("=====================================USER FINSH==========================================");
				
				w.flush();
				w.close();
				
			} catch(AuthenticationException e) {
				String msg = e.getMessage();
				if (msg.indexOf("data 525") > 0) {
					log.info("사용자를 찾을 수 없음.");
			 	} else if (msg.indexOf("data 773") > 0) {
			 		log.info("사용자는 암호를 재설정해야합니다.");
			 	} else if (msg.indexOf("data 52e") > 0) {
			 		log.info("ID와 비밀번호가 일치하지 않습니다.확인 후 다시 시도해 주십시오.");
			 	} else if (msg.indexOf("data 533") > 0) {
			 		log.info("입력한 ID는 비활성화 상태 입니다.");
			 	} else if(msg.indexOf("data 532") > 0){
			 		log.info("암호가 만료되었습니다.");
			 	} else if(msg.indexOf("data 701") > 0){
			 		log.info("AD에서 계정이 만료됨");
			 	} else {
			 		log.info("연결");
			 	}

			 } catch (Exception e) {
				 log.info("Active Directory ERROR : {}", e.getMessage());
			 } finally {
				 log.info("Active Directory Connection: DISCONNECTED");
				 ctx.close();
			 }
		} catch (Exception e) {
			log.info("ERROR : {}", e.getMessage());
		}
	}
	
	private String hashCode(String deptCode) {
		return DigestUtils.md5Hex(deptCode);	
	}
}  

