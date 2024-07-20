package com.nokia.esim.security;

//@Configuration
//@EnableWebSecurity
public class SecurityConfig // extends WebSecurityConfigurerAdapter
{

//    @Value("${security.user.name}")
//    private String username;
//
//    @Value("${security.user.password}")
//    private String password;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception
//    {
//        http
//            .csrf().disable()
//            .authorizeRequests().anyRequest().authenticated()
//            .and().httpBasic()
//            .and()
//            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//    }
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception
//    {
//        auth.inMemoryAuthentication()
//            .withUser(username)
//            .password("{noop}" + password)
//            .roles("USER");
//    }
}
