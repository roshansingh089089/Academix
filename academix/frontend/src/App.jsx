import{useCallback,useEffect,useState}from'react';
import Sidebar from'./components/Sidebar';
import TopNavbar from'./components/TopNavbar';
import Dashboard from'./pages/Dashboard';
import{StudentsPage,ImportPage,Placeholder}from'./pages/OtherPages';
import{dashboard as demo,students as demoStudents}from'./data/demo';
import{getDashboard,getStudents}from'./api';
import{useDebounce}from'./hooks/useDebounce';

export default function App(){
 const[active,setActive]=useState('Dashboard'),[side,setSide]=useState(false),[query,setQuery]=useState('');
 const[data,setData]=useState(demo),[students,setStudents]=useState(demoStudents);
 const[page,setPage]=useState(0),[pageSize,setPageSize]=useState(50),[total,setTotal]=useState(demo.totalStudents);
 const[apiOnline,setApiOnline]=useState(false),[loading,setLoading]=useState(true);
 const debounced=useDebounce(query);

 const loadStudents=useCallback(async(pageNumber=0,search='',size=pageSize)=>{
  setLoading(true);
  try{
   const response=await getStudents({q:search||undefined,page:pageNumber,size,sort:'createdAt',direction:'desc'});
   setStudents(response.content);setTotal(response.totalElements);setPage(response.page);setApiOnline(true);
  }catch(error){
   if(!apiOnline){setStudents(demoStudents);setTotal(demo.totalStudents)}
  }finally{setLoading(false)}
 },[pageSize,apiOnline]);

 const refresh=useCallback(async()=>{
  await Promise.all([
   getDashboard().then(stats=>{setData(stats);setApiOnline(true)}).catch(()=>{}),
   loadStudents(0,debounced,pageSize)
  ]);
 },[loadStudents,debounced,pageSize]);

 useEffect(()=>{refresh()},[]);
 useEffect(()=>{if(query!==''||apiOnline){loadStudents(0,debounced,pageSize);if(debounced)setActive('Students')}},[debounced,pageSize]);

 const tableProps={students,total,page:page+1,size:pageSize,onPageChange:p=>loadStudents(p-1,debounced,pageSize),loading,live:apiOnline};
 let content=active==='Dashboard'
  ?<Dashboard data={data} {...tableProps} navigate={setActive}/>
  :active==='Students'
   ?<StudentsPage {...tableProps} onPageSizeChange={setPageSize}/>
   :active==='Import Data'
    ?<ImportPage onImported={async()=>{await refresh();setActive('Students')}}/>
    :<Placeholder title={active}/>;

 return <div className="app"><Sidebar open={side} onClose={()=>setSide(false)} active={active} onSelect={setActive}/><div className="shell"><TopNavbar query={query} setQuery={setQuery} onMenu={()=>setSide(true)}/><main>{content}</main><footer>© 2026 Academix · Student Data &amp; Coaching Management</footer></div>{side&&<div className="scrim" onClick={()=>setSide(false)}/>}</div>
}
