import axios from'axios';

export const api=axios.create({baseURL:'/api',timeout:15000});
export const getStudents=params=>api.get('/students',{params}).then(r=>r.data);
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
