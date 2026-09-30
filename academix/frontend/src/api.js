import axios from'axios';

export const api=axios.create({baseURL:'/api',timeout:15000});
export const getStudents=params=>api.get('/students',{params}).then(r=>r.data);
export const getStudentFilterOptions=state=>api.get('/students/filter-options',{params:state?{state}:undefined}).then(r=>r.data);
export async function exportStudents(params){
 try{
  const response=await api.get('/students/export',{params,responseType:'blob',timeout:120000});
  const disposition=response.headers['content-disposition']||'';
  const filename=disposition.match(/filename="?([^";]+)"?/i)?.[1]||`academix-students-${new Date().toISOString().slice(0,10)}.csv`;
  const url=URL.createObjectURL(response.data),link=document.createElement('a');
  link.href=url;link.download=filename;document.body.appendChild(link);link.click();link.remove();URL.revokeObjectURL(url);
 }catch(error){
  if(error.response?.data instanceof Blob){try{error.response.data=JSON.parse(await error.response.data.text())}catch{ /* response was not JSON */ }}
  throw error;
 }
}
export const getDashboard=()=>api.get('/dashboard/stats').then(r=>r.data);
export const getImports=params=>api.get('/imports',{params}).then(r=>r.data);
export const uploadStudents=(file,onUploadProgress)=>{const data=new FormData();data.append('file',file);return api.post('/imports',data,{onUploadProgress,timeout:120000}).then(r=>r.data)};

export function getApiErrorMessage(error){
 if(!error?.response){
  if(error?.code==='ECONNABORTED')return 'The backend took too long to respond. Please try again.';
  return 'The Academix backend is unavailable. Check the server connection and try again.';
 }
 const message=error.response.data?.message;
 if(error.response.status===400)return message||'The request contains invalid data.';
 if(error.response.status===404)return message||'The requested record was not found.';
 if(error.response.status>=500)return message?`Server error: ${message}`:'The backend could not complete the request.';
 return message||`Request failed with status ${error.response.status}.`;
}
