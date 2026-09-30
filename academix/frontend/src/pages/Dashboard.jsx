import{Users,UserCheck,UserMinus,BookOpen,Layers3,UploadCloud,CalendarDays,Plus}from'lucide-react';
import StatCard from'../components/StatCard';
import{CourseChart,CityChart}from'../components/Charts';
import RecentImports from'../components/RecentImports';
import StudentTable from'../components/StudentTable';

export default function Dashboard({data,students,total,page,size,onPageChange,loading,error,navigate,navigateStudents}){
 const stats=[
  ['Total Students',data.totalStudents,'All student records',Users,'blue',()=>navigateStudents()],
  ['Active Students',data.activeStudents,'Currently active',UserCheck,'green',()=>navigateStudents({status:'ACTIVE'})],
  ['Inactive Students',data.inactiveStudents,'Currently inactive',UserMinus,'orange',()=>navigateStudents({status:'INACTIVE'})],
  ['Total Courses',data.totalCourses,'Browse by course',BookOpen,'purple',()=>navigateStudents()],
  ['Total Batches',data.totalBatches,'Browse by batch',Layers3,'cyan',()=>navigateStudents()],
  ['Total Imports',data.totalImports,'View import history',UploadCloud,'indigo',()=>navigate('Import History')]
 ];
 return <><div className="page-heading"><div><p className="eyebrow">WORKSPACE OVERVIEW</p><h1>Dashboard</h1><p>Overview of your student data and recent activity</p></div><div className="heading-actions"><button className="date-button" disabled title="Date-range filtering is not available yet"><CalendarDays size={17}/> All time</button><button className="primary" onClick={()=>navigateStudents()}><Plus size={17}/> Manage students</button></div></div>
 <div className="stats-grid">{stats.map(([label,value,note,icon,tone,onClick])=><StatCard key={label} label={label} value={value} note={note} icon={icon} tone={tone} onClick={onClick}/>)}</div>
 <div className="analytics-grid"><CourseChart rows={data.courses||[]} total={Number(data.totalStudents)||1} onViewReport={()=>navigateStudents()}/><CityChart rows={data.cities||[]}/><RecentImports rows={data.recentImports||[]} onViewAll={()=>navigate('Import History')} onOpen={()=>navigate('Import History')}/></div>
 <StudentTable students={students} total={total} page={page} size={size} onPageChange={onPageChange} loading={loading} error={error} onViewAll={()=>navigateStudents()}/></>
}
