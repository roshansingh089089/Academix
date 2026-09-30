import{useCallback,useEffect,useState}from'react';
import Sidebar from'./components/Sidebar';
import TopNavbar from'./components/TopNavbar';
import Dashboard from'./pages/Dashboard';
import{StudentsPage,ImportPage,ImportHistoryPage,Placeholder}from'./pages/OtherPages';
import{exportStudents,getApiErrorMessage,getDashboard,getStudents}from'./api';
import{useDebounce}from'./hooks/useDebounce';

const emptyDashboard={totalStudents:0,activeStudents:0,inactiveStudents:0,totalCourses:0,totalBatches:0,totalImports:0,courses:[],cities:[],recentImports:[]};
const emptyFilters={fullName:'',phone:'',email:'',studentCode:'',schoolName:'',city:'',state:'',className:'',course:'',batch:'',status:'',source:'',ageFrom:'',ageTo:'',admissionFrom:'',admissionTo:''};

export default function App(){
 const[active,setActive]=useState('Dashboard'),[side,setSide]=useState(false),[query,setQueryState]=useState('');
 const[filters,setFilters]=useState(emptyFilters),[sort,setSort]=useState('createdAt'),[direction,setDirection]=useState('desc');
 const[data,setData]=useState(emptyDashboard),[students,setStudents]=useState([]);
 const[page,setPage]=useState(0),[pageSize,setPageSize]=useState(50),[total,setTotal]=useState(0);
 const[loading,setLoading]=useState(true),[error,setError]=useState(''),[refreshKey,setRefreshKey]=useState(0);
 const debounced=useDebounce(query);

 const refreshDashboard=useCallback(()=>getDashboard().then(setData).catch(e=>setError(apiMessage(e))),[]);
 useEffect(()=>{refreshDashboard()},[refreshDashboard,refreshKey]);
 useEffect(()=>{
  let activeRequest=true;setLoading(true);setError('');
  getStudents({q:debounced||undefined,...clean(filters),page,size:pageSize,sort,direction})
   .then(response=>{if(activeRequest){setStudents(response.content);setTotal(response.totalElements)}})
   .catch(e=>{if(activeRequest){setStudents([]);setTotal(0);setError(apiMessage(e))}})
   .finally(()=>{if(activeRequest)setLoading(false)});
  return()=>{activeRequest=false};
 },[debounced,filters,page,pageSize,sort,direction,refreshKey]);

 const setQuery=value=>{setQueryState(value);setPage(0)};
 const changeFilter=(name,value)=>{setFilters(current=>({...current,[name]:value}));setPage(0)};
 const changeState=value=>{setFilters(current=>({...current,state:value,city:''}));setPage(0)};
 const clearFilters=()=>{setFilters(emptyFilters);setQueryState('');setSort('createdAt');setDirection('desc');setPage(0)};
 const changeSort=value=>{const[field,order]=value.split(':');setSort(field);setDirection(order);setPage(0)};
 const exportFilteredStudents=()=>exportStudents({q:query||undefined,...clean(filters),sort,direction});
 const navigateStudents=preset=>{setActive('Students');if(preset){setFilters(current=>({...current,...preset}));setPage(0)}};
 const afterImport=()=>{setRefreshKey(key=>key+1);setActive('Students');setPage(0)};
 const tableProps={students,total,page:page+1,size:pageSize,onPageChange:p=>setPage(p-1),loading,error};

 let content=active==='Dashboard'
  ?<Dashboard data={data} {...tableProps} navigate={setActive} navigateStudents={navigateStudents}/>
  :active==='Students'
   ?<StudentsPage {...tableProps} query={query} onQueryChange={setQuery} filters={filters} onFilterChange={changeFilter} onStateChange={changeState} onClear={clearFilters} sort={`${sort}:${direction}`} onSortChange={changeSort} onExport={exportFilteredStudents} onPageSizeChange={size=>{setPageSize(size);setPage(0)}}/>
   :active==='Import Data'
    ?<ImportPage onImported={afterImport}/>
    :active==='Import History'
     ?<ImportHistoryPage/>
     :<Placeholder title={active}/>;

 return <div className="app"><Sidebar open={side} onClose={()=>setSide(false)} active={active} onSelect={setActive}/><div className="shell"><TopNavbar query={query} setQuery={setQuery} onMenu={()=>setSide(true)}/><main>{content}</main><footer>© 2026 Academix · Student Data &amp; Coaching Management</footer></div>{side&&<div className="scrim" onClick={()=>setSide(false)}/>}</div>
}

function clean(values){return Object.fromEntries(Object.entries(values).filter(([,value])=>value!==''&&value!=null))}
function apiMessage(error){return getApiErrorMessage(error)}
