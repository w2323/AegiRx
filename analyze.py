import os
import re
import json

def analyze_java_files(root_dir):
    results = {
        'inheritance': [],
        'composition_aggregation': [],
        'grasp': []
    }
    
    for subdir, _, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(subdir, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                        
                    class_name = ""
                    for i, line in enumerate(lines):
                        line_num = i + 1
                        
                        # Inheritance
                        match = re.search(r'class\s+(\w+)\s+(extends|implements)\s+([\w\s,]+)', line)
                        if match:
                            class_name = match.group(1)
                            results['inheritance'].append({
                                'file': path,
                                'class': class_name,
                                'type': match.group(2),
                                'parent': match.group(3).strip().replace('{','').strip(),
                                'line': line_num
                            })
                            continue
                            
                        # Keep track of class name
                        match_class = re.search(r'class\s+(\w+)', line)
                        if match_class and not class_name:
                            class_name = match_class.group(1)
                            
                        # Composition/Aggregation (Fields that are other domain objects or collections)
                        match_field = re.search(r'private\s+(List<[\w]+>|Set<[\w]+>|Map<[\w,\s]+>|[A-Z]\w+)\s+(\w+);', line)
                        if match_field and not 'String' in match_field.group(1) and not 'LocalDate' in match_field.group(1) and not 'Double' in match_field.group(1) and not 'Integer' in match_field.group(1):
                            field_type = match_field.group(1)
                            field_name = match_field.group(2)
                            # Exclude primitive wrappers and standard types
                            if field_type not in ['int', 'double', 'float', 'long', 'boolean']:
                                results['composition_aggregation'].append({
                                    'file': path,
                                    'class': class_name,
                                    'field': field_name,
                                    'type': field_type,
                                    'line': line_num
                                })
                                
                        # GRASP Patterns
                        # Controller
                        if 'Controller' in path or 'controller' in path.lower() or class_name.endswith('Controller'):
                            if class_name and not any(r['class'] == class_name and r['pattern'] == 'Controller' for r in results['grasp']):
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Controller',
                                    'line': line_num,
                                    'explanation': 'Handles UI events and delegates work to services.'
                                })
                                
                        # Creator (Factory)
                        if 'Factory' in path or class_name.endswith('Factory') or 'Generator' in class_name:
                            if class_name and not any(r['class'] == class_name and r['pattern'] == 'Creator' for r in results['grasp']):
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Creator',
                                    'line': line_num,
                                    'explanation': 'Responsible for creating objects (e.g. Factory pattern).'
                                })
                                
                        # Information Expert / High Cohesion (DAO, Service)
                        if 'DAO' in path or class_name.endswith('DAO') or 'Service' in path or class_name.endswith('Service'):
                            if class_name and not any(r['class'] == class_name and r['pattern'] in ['Information Expert', 'High Cohesion'] for r in results['grasp']):
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Information Expert / High Cohesion',
                                    'line': line_num,
                                    'explanation': 'Manages specific domain data operations or business logic, exhibiting high cohesion.'
                                })

                        # Indirection / Polymorphism (Interfaces, Service Locator, etc.)
                        if 'Observer' in path or class_name.endswith('Observer'):
                            if class_name and not any(r['class'] == class_name and r['pattern'] == 'Polymorphism' for r in results['grasp']):
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Polymorphism',
                                    'line': line_num,
                                    'explanation': 'Uses polymorphism for event handling or observation.'
                                })
                except Exception as e:
                    pass
                    
    print(json.dumps(results, indent=2))

analyze_java_files('d:\\UND\\src')
